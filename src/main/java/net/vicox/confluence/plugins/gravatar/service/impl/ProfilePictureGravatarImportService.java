package net.vicox.confluence.plugins.gravatar.service.impl;

import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.confluence.user.PersonalInformation;
import com.atlassian.confluence.user.PersonalInformationManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.user.actions.ProfilePictureInfo;
import com.atlassian.core.exception.InfrastructureException;
import com.atlassian.user.User;
import net.vicox.confluence.plugins.gravatar.service.GravatarImportService;
import net.vicox.confluence.plugins.gravatar.service.GravatarSettingsService;
import net.vicox.confluence.plugins.gravatar.util.GravatarUtil;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Imports the user's Gravatar as a Confluence profile picture.
 *
 * @author Georg Schmidl
 */
public class ProfilePictureGravatarImportService implements GravatarImportService {

    private static final Logger log = LoggerFactory.getLogger(ProfilePictureGravatarImportService.class);

    private static final String INTERNAL_FILE_NAME_PREFIX = "gravatar-";

    private PersonalInformationManager personalInformationManager;
    private AttachmentManager attachmentManager;
    private UserAccessor userAccessor;
    private GravatarSettingsService gravatarSettingsService;

    @Override
    public void importGravatar(User user) throws IOException {
        String gravatarServerUrl  = gravatarSettingsService.getSettings().get("gravatarServerUrl");
        byte[] gravatarData = GravatarUtil.loadGravatarImage(gravatarServerUrl, user.getEmail());
        String gravatarFileName = getInternalGravatarFileName(gravatarData);

        Attachment gravatarAttachment = getGravatarAttachment(user);

        if (gravatarAttachment == null) {
            log.debug("setting gravatar as profile picture for user {}", user.getName());
            addAttachment(user, gravatarData, gravatarFileName);

        } else if (!gravatarAttachment.getFileName().equals(gravatarFileName)) {
            log.debug("updating the gravatar profile picture with new gravatar for user {}", user.getName());
            updateAttachment(user, gravatarData, gravatarFileName, gravatarAttachment);

        } else {
            log.debug("updating the gravatar profile pictures last modification date for user {}", user.getName());
            touchAttachment(user, gravatarAttachment);
        }
    }

    private void addAttachment(User user, byte[] gravatarData, String gravatarFileName) {
        Attachment gravatarAttachment = saveOrUpdateUserAttachment(user, gravatarData, gravatarFileName);
        userAccessor.setUserProfilePicture(user, gravatarAttachment);
    }

    private void updateAttachment(User user, byte[] gravatarData, String gravatarFileName, Attachment gravatarAttachment) {
        PersonalInformation userPersonalInformation = personalInformationManager.getOrCreatePersonalInformation(user);
        attachmentManager.moveAttachment(gravatarAttachment, gravatarFileName, userPersonalInformation);
        gravatarAttachment = saveOrUpdateUserAttachment(user, gravatarData, gravatarFileName);
        userAccessor.setUserProfilePicture(user, gravatarAttachment);
    }

    private void touchAttachment(User user, Attachment gravatarAttachment) {
        gravatarAttachment.setLastModificationDate(new Date());
        userAccessor.setUserProfilePicture(user, gravatarAttachment);
    }

    private Attachment saveOrUpdateUserAttachment(User user, byte[] imageData, String imageFileName) {
        PersonalInformation userPersonalInformation = this.personalInformationManager.getOrCreatePersonalInformation(user);
        Attachment attachment = this.attachmentManager.getAttachment(userPersonalInformation, imageFileName);

        Attachment previousVersion = null;

        if (attachment == null) {
            attachment = new Attachment();
        } else {
            try {
                previousVersion = (Attachment) attachment.clone();
            } catch (Exception e) {
                throw new InfrastructureException(e);
            }
        }

        attachment.setContentType("image/png");
        attachment.setFileName(imageFileName);
        attachment.setComment("Imported Gravatar Picture");
        attachment.setFileSize(imageData.length);
        userPersonalInformation.addAttachment(attachment);

        InputStream is = new ByteArrayInputStream(imageData);
        try {
            this.attachmentManager.saveAttachment(attachment, previousVersion, is);
        } catch (IOException e) {
            throw new InfrastructureException("Error saving attachment data: " + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(is);
        }

        return attachment;
    }

    @Override
    public void removeGravatar(User user) {
        // should be implemented when Confluence gets a delete avatar function
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean usesGravatar(User user) {
        boolean usesGravatar = false;

        ProfilePictureInfo profilePictureInfo = userAccessor.getUserProfilePicture(user);

        if (profilePictureInfo != null) {
            String filename = profilePictureInfo.getFileName();

            if (filename != null && isInternalGravatarFileName(filename)) {
                usesGravatar = true;
            }
        }

        return usesGravatar;
    }

    @Override
    public String getImportedPicturePath(User user) {
        Attachment attachment = getGravatarAttachment(user);
        if (attachment != null) {
            return attachment.getDownloadPath();
        }
        return null;
    }

    @Override
    public Date getLastImportedDate(User user) {
        Attachment attachment = getGravatarAttachment(user);
        if (attachment != null) {
            return attachment.getLastModificationDate();
        }
        return null;
    }

    private Attachment getGravatarAttachment(User user) {
        if (personalInformationManager.hasPersonalInformation(user.getName())) {
            PersonalInformation userPersonalInformation = personalInformationManager.getOrCreatePersonalInformation(user);

            for (Attachment attachment : attachmentManager.getLatestVersionsOfAttachments(userPersonalInformation)) {
                if (isInternalGravatarFileName(attachment.getFileName())) {
                    return attachment;
                }
            }
        }
        return null;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    public void setPersonalInformationManager(PersonalInformationManager personalInformationManager) {
        this.personalInformationManager = personalInformationManager;
    }

    public void setUserAccessor(UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }

    public void setGravatarSettingsService(GravatarSettingsService gravatarSettingsService) {
        this.gravatarSettingsService = gravatarSettingsService;
    }

    private static String getInternalGravatarFileName(byte[] image) {
        return INTERNAL_FILE_NAME_PREFIX + generateImageHash(image) + ".png";
    }

    private static boolean isInternalGravatarFileName(String fileName) {
        return fileName.startsWith(INTERNAL_FILE_NAME_PREFIX);
    }

    private static String generateImageHash(byte[] image) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] hash = messageDigest.digest(image);
            return new String(Hex.encodeHex(hash));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
