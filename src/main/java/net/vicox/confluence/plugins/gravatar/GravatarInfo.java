package net.vicox.confluence.plugins.gravatar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Provides information about the user's Gravatar.
 *
 * @author Georg Schmidl
 */
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class GravatarInfo {

    private String gravatarUrl;
    private String importedPicturePath;

    public String getGravatarUrl() {
        return gravatarUrl;
    }

    public void setGravatarUrl(String gravatarUrl) {
        this.gravatarUrl = gravatarUrl;
    }

    public String getImportedPicturePath() {
        return importedPicturePath;
    }

    public void setImportedPicturePath(String importedPicturePath) {
        this.importedPicturePath = importedPicturePath;
    }
}