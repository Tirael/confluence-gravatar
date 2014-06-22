AJS.$.get(AJS.contextPath() + '/rest/gravatar/latest/info', function(info){
    var template;

    if(info.importedPicturePath) {
        template = '\
            <form name="importgravatar" method="POST" action="{0}" enctype="application/x-www-form-urlencoded" class="aui long-label"> \
                <h2>{3}</h2> \
                <fieldset class="default-icon-set"> \
                <div class="default-user-pics gravatar"> \
                    <label for="userProfilePictureName.gravatar-imported"> \
                        <img src="{2}" class="userphoto" alt="Available picture: gravatar-imported"> \
                    </label> \
                </div> \
                <div class="default-user-pics-buttons"> \
                    <input class="aui-button" type="submit" name="save" value="{6}">\
                    <input class="aui-button" type="submit" name="delete" value="{5}">\
                </div>\
                </fieldset> \
            </form>';
        } else {
        template = '\
            <form name="importgravatar" method="POST" action="{0}" enctype="application/x-www-form-urlencoded" class="aui long-label"> \
                <h2>{3}</h2> \
                <fieldset class="default-icon-set"> \
                    <div class="default-user-pics gravatar"> \
                        <label for="userProfilePictureName.gravatar-original.png"> \
                            <img src="{1}" class="userphoto" alt="Available picture: gravatar-original"> \
                        </label> \
                    </div> \
                    <div class="default-user-pics-buttons"> \
                        <input class="aui-button aui-button-primary" type="submit" name="save" value="{4}">\
                    </div>\
                </fieldset> \
            </form>';
        }

    if (info.importedPicturePath || info.gravatarUrl) {
        AJS.$(document).ready(function(){
            AJS.$('.edit-my-picture-profile').append(AJS.format(template,
                /* 0 */ AJS.contextPath() + '/plugins/gravatar/import.action',
                /* 1 */ info.gravatarUrl,
                /* 2 */ AJS.contextPath() + info.importedPicturePath,
                /* 3 */ AJS.I18n.getText('gravatar.header'),
                /* 4 */ AJS.I18n.getText('gravatar.import.button'),
                /* 5 */ AJS.I18n.getText('gravatar.delete.button'),
                /* 6 */ AJS.I18n.getText('gravatar.update.button')
            ));
        });
    }

});