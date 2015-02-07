(function ($) {
    var uploaderTemplate = '\
            <div id="gravatar-importer"> \
                <button class="aui-button" id="gravatar-importer-button">{3}</button>\
                <section role="dialog" id="gravatar-importer-dialog" class="aui-layer aui-dialog2 aui-dialog2-small" aria-hidden="true">\
                    <header class="aui-dialog2-header">\
                        <h2 class="aui-dialog2-header-main">{4}</h2>\
                    </header>\
                    <div class="aui-dialog2-content">\
                        <img src="{1}">\
                    </div>\
                    <footer class="aui-dialog2-footer">\
                        <div class="aui-dialog2-footer-actions">\
                            <button id="gravatar-importer-submit-button"  class="aui-button aui-button-primary">{5}</button>\
                            <button id="gravatar-importer-close-button" class="aui-button aui-button-link">{6}</button>\
                    </div>\
                    </footer>\
                </section>\
            </div>';

    var formImportTemplate = '\
            <form name="importgravatar" method="POST" action="{0}" enctype="application/x-www-form-urlencoded" class="aui long-label"> \
                <h2>{3}</h2> \
                <fieldset class="default-icon-set"> \
                    <div class="default-user-pics gravatar"> \
                        <label for="userProfilePictureName.gravatar-original.png"> \
                            <img src="{1}" class="userphoto" alt="Available picture: gravatar-original"> \
                        </label> \
                    </div> \
                    <div class="default-user-pics-buttons"> \
                        <input class="aui-button" type="submit" name="save" value="{4}">\
                    </div>\
                </fieldset> \
            </form>';

    var formUpdateTemplate = '\
            <form name="importgravatar" method="POST" action="{0}" enctype="application/x-www-form-urlencoded" class="aui long-label"> \
                <h2>{3}</h2> \
                <fieldset class="default-icon-set"> \
                <div class="default-user-pics gravatar"> \
                    <label for="userProfilePictureName.gravatar-imported"> \
                        <img src="{1}" class="userphoto" alt="Available picture: gravatar-imported"> \
                    </label> \
                </div> \
                <div class="default-user-pics-buttons"> \
                    <input class="aui-button" type="submit" name="save" value="{6}">\
                    <input class="aui-button" type="submit" name="delete" value="{5}">\
                </div>\
                </fieldset> \
            </form>';

    $.get(AJS.contextPath() + '/rest/gravatar/latest/info', function(info){
        if (info.importedPicturePath || info.gravatarUrl) {

            $(document).ready(function(){
                var $avatarUploader = $('.edit-my-profile-picture .uploader-section .aui-item:first-child');

                if ($avatarUploader.length > 0) {
                    $avatarUploader.append(AJS.format(uploaderTemplate,
                        /* 0 */ AJS.contextPath() + '/plugins/gravatar/import.action',
                        /* 1 */ info.gravatarUrl,
                        /* 2 */ AJS.contextPath() + info.importedPicturePath,
                        /* 3 */ AJS.I18n.getText('gravatar.uploader.import.button'),
                        /* 4 */ AJS.I18n.getText('gravatar.uploader.import.title'),
                        /* 5 */ AJS.I18n.getText('gravatar.uploader.import.dialog.button'),
                        /* 6 */ AJS.I18n.getText('gravatar.uploader.cancel.dialog.button')
                    ));

                    $('#gravatar-importer-button').click(function(e) {
                        e.preventDefault();
                        AJS.dialog2("#gravatar-importer-dialog").show();
                    });

                    $("#gravatar-importer-close-button").click(function(e) {
                        e.preventDefault();
                        AJS.dialog2("#gravatar-importer-dialog").hide();
                    });

                    $("#gravatar-importer-submit-button").click(function(e) {
                        e.preventDefault();
                        $.get(AJS.contextPath() + '/rest/gravatar/latest/import', function(info){
                            $('img.user-avatar-preview, img.userLogo, .aui-avatar-inner img')
                                .attr('src', AJS.contextPath() + info.importedPicturePath);
                            AJS.dialog2("#gravatar-importer-dialog").hide();
                        }).fail(function() {
                            alert(AJS.I18n.getText('gravatar.uploader.error'));
                        })
                    });

                } else { // Confluence < 5.7
                    var formTemplate = info.importedPicturePath ? formUpdateTemplate : formImportTemplate;

                    $('.edit-my-picture-profile').append(AJS.format(formTemplate,
                        /* 0 */ AJS.contextPath() + '/plugins/gravatar/import.action',
                        /* 1 */ info.gravatarUrl,
                        /* 2 */ AJS.contextPath() + info.importedPicturePath,
                        /* 3 */ AJS.I18n.getText('gravatar.header'),
                        /* 4 */ AJS.I18n.getText('gravatar.import.button'),
                        /* 5 */ AJS.I18n.getText('gravatar.delete.button'),
                        /* 6 */ AJS.I18n.getText('gravatar.update.button')
                    ));
                }
            });
        }

    });

})(AJS.$);