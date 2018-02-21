AJS.toInit(function($) {
  $('.gravatar-settings').each(function() {
    var $gravatarSettings = $(this);

    var $spinner = $gravatarSettings.find('> .spinner');
    var $form = $gravatarSettings.find('> form');

    var $submitButton = $form.find('.submit');
    var $formSpinner = $form.find('.spinner');
    var $successIcon = $form.find('.success');

    var url = $gravatarSettings.data('url');

    var disableForm = function() {
      $form.find('input').attr('disabled', 'disabled');
      $form.find('a').css('pointer-events', 'none');
      $formSpinner.show();
    };

    var enableForm = function() {
      $form.find('input').removeAttr('disabled');
      $form.find('a').css('pointer-events', 'auto');
      $formSpinner.hide();
    };

    var fillValuesIntoForm = function(values) {
      Object.keys(values).forEach(function(key) {
        $('#' + key).val(values[key]);
      });
    };

    $.ajax({
      url: url,
      success: function(initialValues){
        fillValuesIntoForm(initialValues);
        $spinner.hide();
        $form.show();

        $submitButton.click(function(e) {
          e.preventDefault();

          var newValues = $form.serializeArray().reduce(function(obj, item) {
            obj[item.name] = item.value;
            return obj;
          }, {});

          disableForm();

          $.ajax({
            url: url,
            type: 'PUT',
            data: JSON.stringify(newValues),
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            success: function(storedValues) {
              fillValuesIntoForm(storedValues);
              enableForm();
              $successIcon.show();
              setTimeout(function() {
                $successIcon.fadeOut('slow');
              }, 1000);
            }
          });

        });
      }
    });
  });
});