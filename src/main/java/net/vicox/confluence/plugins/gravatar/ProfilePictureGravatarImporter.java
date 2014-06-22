package net.vicox.confluence.plugins.gravatar;

import com.atlassian.confluence.core.service.ServiceCommand;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.confluence.user.PersonalInformation;
import com.atlassian.confluence.user.PersonalInformationManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.user.service.DeleteProfilePictureCommandImpl;
import com.atlassian.confluence.user.service.SetProfilePictureFromImageCommandImpl;
import com.atlassian.user.User;
import org.apache.commons.codec.binary.Hex;
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
public class ProfilePictureGravatarImporter implements GravatarImporter {

    private static final Logger log = LoggerFactory.getLogger(ProfilePictureGravatarImporter.class);

    private static final String INTERNAL_FILE_NAME_PREFIX = "gravatar-";

    protected PersonalInformationManager personalInformationManager;
    protected AttachmentManager attachmentManager;
    protected UserAccessor userAccessor;

    @Override
    public void importGravatar(User user) throws IOException {
        byte[] newGravatar = GravatarUtil.loadGravatarImage(user.getEmail());
        String newGravatarFileName = getInternalGravatarFileName(newGravatar);

        Attachment oldGravatarAttachment = getGravatarAttachment(user);

        if (oldGravatarAttachment == null) {
            log.debug("setting gravatar as profile picture for user {}", user.getName());
            newSetProfilePictureCommand(user, new ByteArrayInputStream(newGravatar), newGravatarFileName).execute();

        } else if (!oldGravatarAttachment.getFileName().equals(newGravatarFileName)) {
            log.debug("updating the gravatar profile picture with new gravatar for user {}", user.getName());
            PersonalInformation userPersonalInformation = personalInformationManager.getOrCreatePersonalInformation(user);
            attachmentManager.moveAttachment(oldGravatarAttachment, newGravatarFileName, userPersonalInformation);
            newSetProfilePictureCommand(user, new ByteArrayInputStream(newGravatar), newGravatarFileName).execute();

        } else {
            log.debug("updating the gravatar profile pictures last modification date for user {}", user.getName());
            oldGravatarAttachment.setLastModificationDate(new Date());
        }
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

    @Override
    public void removeGravatar(User user) {
        log.debug("removing gravatar profile picture for user {}", user.getName());
        Attachment gravatarAttachment = getGravatarAttachment(user);
        if (gravatarAttachment != null) {
            newDeleteProfilePictureCommand(user, gravatarAttachment.getFileName()).execute();
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
        PersonalInformation userPersonalInformation = personalInformationManager.getOrCreatePersonalInformation(user);

        for(Attachment attachment: attachmentManager.getLatestVersionsOfAttachments(userPersonalInformation)) {
            if (isInternalGravatarFileName(attachment.getFileName())) {
                return attachment;
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
