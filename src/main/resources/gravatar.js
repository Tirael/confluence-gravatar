AJS.$.get(AJS.contextPath() + '/rest/gravatar/latest/userinfo', function(info){
    if(info.url) {
        var useGravatarTemplate = '\
            <form name="usegravatar" method="POST" action="{0}" enctype="application/x-www-form-urlencoded" class="aui long-label"> \
                <h2>{1}</h2> \
                <fieldset class="default-icon-set"> \
                    <div class="default-user-pics gravatar"> \
                        <label for="userProfilePictureName.gravatar.png"> \
                            <img src="{2}" class="userphoto {3}" alt="Available picture: gravatar.png"> \
                        </label> \
                    </div> \
                    <div class="default-user-pics-buttons"> \
                        <input type="submit" name="save" value="{4}"> \
                    </div> \
                </fieldset> \
            </form>';

        AJS.$(document).ready(function(){
            AJS.$('.edit-my-picture-profile').append(AJS.format(useGravatarTemplate,
                AJS.contextPath() + '/plugins/gravatar/use.action',
                AJS.I18n.getText('gravatar.use'),
                info.url,
                info.using?'userphoto-selected':'',
                AJS.I18n.getText('gravatar.use.button')
            ));
        });
    }
});