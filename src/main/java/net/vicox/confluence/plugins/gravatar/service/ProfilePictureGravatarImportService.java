package net.vicox.confluence.plugins.gravatar.service;

import com.atlassian.confluence.core.service.ServiceCommand;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.confluence.user.PersonalInformation;
import com.atlassian.confluence.user.PersonalInformationManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.user.service.DeleteProfilePictureCommandImpl;
import com.atlassian.confluence.user.service.SetProfilePictureFromImageCommandImpl;
import com.atlassian.core.exception.InfrastructureException;
import com.atlassian.user.User;
import net.vicox.confluence.plugins.gravatar.util.GravatarUtil;
import net.vicox.confluence.plugins.gravatar.util.SystemUtil;
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

    protected PersonalInformationManager personalInformationManager;
    protected AttachmentManager attachmentManager;
    protected UserAccessor userAccessor;

    @Override
    public void importGravatar(User user) throws IOException {
        byte[] gravatarData = GravatarUtil.loadGravatarImage(user.getEmail());
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

    protected void addAttachment(User user, byte[] gravatarData, String gravatarFileName) {
        if (SystemUtil.profilePictureCommandIsDeprecated()) {
            Attachment gravatarAttachment = saveOrUpdateUserAttachment(user, gravatarData, gravatarFileName);
            userAccessor.setUserProfilePicture(user, gravatarAttachment);

        } else { // Confluence < 5.7
            newSetProfilePictureCommand(user, new ByteArrayInputStream(gravatarData), gravatarFileName).execute();
        }
    }

    protected void updateAttachment(User user, byte[] gravatarData, String gravatarFileName, Attachment gravatarAttachment) {
        PersonalInformation userPersonalInformation = personalInformationManager.getOrCreatePersonalInformation(user);
        attachmentManager.moveAttachment(gravatarAttachment, gravatarFileName, userPersonalInformation);

        if (SystemUtil.profilePictureCommandIsDeprecated()) {
            gravatarAttachment = saveOrUpdateUserAttachment(user, gravatarData, gravatarFileName);
            userAccessor.setUserProfilePicture(user, gravatarAttachment);

        } else { // Confluence < 5.7
            newSetProfilePictureCommand(user, new ByteArrayInputStream(gravatarData), gravatarFileName).execute();
        }
    }

    protected void touchAttachment(User user, Attachment gravatarAttachment) {
        gravatarAttachment.setLastModificationDate(new Date());
        userAccessor.setUserProfilePicture(user, gravatarAttachment);
    }

    protected ServiceCommand newSetProfilePictureCommand(User user, InputStream imageData, String imageFileName) {
        return new SetProfilePictureFromImageCommandImpl(null,
                personalInformationManager,
                userAccessor,
                attachmentManager,
                user,
                imageData,
                imageFileName) {
            @Override
            protected boolean isAuthorizedInternal() {
                return true;
            }
        };
    }

    protected Attachment saveOrUpdateUserAttachment(User user, byte[] imageData, String imageFileName) {
        PersonalInformation userPersonalInformation = this.personalInformationManager.getOrCreatePersonalInformation(user);
        Attachment attachment = this.attachmentManager.getAttachment(userPersonalInformation, imageFileName);

        Attachment previousVersion = null;

        if (attachment == null) {
            attachment = new Attachment();
        } else {
            try {
                previousVersion = (Attachment) attachment.clone();
            } catch (CloneNotSupportedException e) {
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
        if (SystemUtil.profilePictureCommandIsDeprecated()) {
            // should be implemented when Confluence gets a delete avatar function
            throw new UnsupportedOperationException();

        } else { // Confluence < 5.7
            log.debug("removing gravatar profile picture for user {}", user.getName());
            Attachment gravatarAttachment = getGravatarAttachment(user);
            if (gravatarAttachment != null) {
                newDeleteProfilePictureCommand(user, gravatarAttachment.getFileName()).execute();
            }
        }
    }

    protected ServiceCommand newDeleteProfilePictureCommand(User user, String imageFileName){
        return new DeleteProfilePictureCommandImpl(null,
                personalInformationManager,
                userAccessor,
                attachmentManager,
                user,
                imageFileName) {
            @Override
            protected boolean isAuthorizedInternal() {
                return true;
            }
        };
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

    protected Attachment getGravatarAttachment(User user) {
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

    protected static String getInternalGravatarFileName(byte[] image) {
        return INTERNAL_FILE_NAME_PREFIX + generateImageHash(image) + ".png";
    }

    protected static boolean isInternalGravatarFileName(String fileName) {
        return fileName.startsWith(INTERNAL_FILE_NAME_PREFIX);
    }

    protected static String generateImageHash(byte[] image) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] hash = messageDigest.digest(image);
            return new String(Hex.encodeHex(hash));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
