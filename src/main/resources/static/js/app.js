(function () {
    var THEME_KEY = 'theme';
    var MAX_PHOTO_BYTES = 2 * 1024 * 1024;
    var root = document.documentElement;

    function currentTheme() {
        return root.getAttribute('data-bs-theme') === 'dark' ? 'dark' : 'light';
    }

    function renderThemeIcons() {
        var icon = currentTheme() === 'dark' ? 'bi-sun' : 'bi-moon-stars';
        document.querySelectorAll('[data-theme-toggle] i').forEach(function (element) {
            element.className = 'bi ' + icon;
        });
    }

    function toggleTheme() {
        var next = currentTheme() === 'dark' ? 'light' : 'dark';
        root.setAttribute('data-bs-theme', next);
        try {
            localStorage.setItem(THEME_KEY, next);
        } catch (e) {
        }
        renderThemeIcons();
        document.dispatchEvent(new CustomEvent('themechange'));
    }

    function showToasts() {
        document.querySelectorAll('.toast').forEach(function (element) {
            bootstrap.Toast.getOrCreateInstance(element, {delay: 4500}).show();
        });
    }

    function setupConfirm() {
        var modalElement = document.getElementById('confirmModal');
        if (!modalElement) {
            return;
        }
        var modal = bootstrap.Modal.getOrCreateInstance(modalElement);
        var message = modalElement.querySelector('[data-confirm-message]');
        var accept = modalElement.querySelector('[data-confirm-accept]');
        var pendingForm = null;

        document.addEventListener('click', function (event) {
            var trigger = event.target.closest('[data-confirm]');
            if (!trigger) {
                return;
            }
            event.preventDefault();
            pendingForm = trigger.closest('form');
            message.textContent = trigger.getAttribute('data-confirm');
            modal.show();
        });

        accept.addEventListener('click', function () {
            if (pendingForm) {
                pendingForm.submit();
            }
        });
    }

    function setupTooltips() {
        document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(function (element) {
            bootstrap.Tooltip.getOrCreateInstance(element);
        });
    }

    function setupTableFilter() {
        document.querySelectorAll('[data-table-filter]').forEach(function (input) {
            var table = document.querySelector(input.getAttribute('data-table-filter'));
            if (!table) {
                return;
            }
            var counter = document.querySelector(input.getAttribute('data-table-count'));
            var emptyRow = table.querySelector('[data-filter-empty]');
            var rows = Array.prototype.slice.call(table.querySelectorAll('tbody tr[data-row]'));

            function apply() {
                var query = input.value.trim().toLowerCase();
                var visible = 0;
                rows.forEach(function (row) {
                    var match = row.textContent.toLowerCase().indexOf(query) !== -1;
                    row.hidden = !match;
                    if (match) {
                        visible++;
                    }
                });
                if (counter) {
                    counter.textContent = visible;
                }
                if (emptyRow) {
                    emptyRow.hidden = visible !== 0 || rows.length === 0;
                }
            }

            input.addEventListener('input', apply);
            apply();
        });
    }

    function setupPhotoInput() {
        var input = document.querySelector('[data-photo-input]');
        if (!input) {
            return;
        }
        var zone = input.closest('.dropzone');
        var preview = document.querySelector('[data-photo-preview]');
        var icon = document.querySelector('[data-photo-icon]');

        ['dragenter', 'dragover'].forEach(function (name) {
            zone.addEventListener(name, function () {
                zone.classList.add('is-dragover');
            });
        });
        ['dragleave', 'drop'].forEach(function (name) {
            zone.addEventListener(name, function () {
                zone.classList.remove('is-dragover');
            });
        });

        input.addEventListener('change', function () {
            var file = input.files[0];
            if (!file) {
                return;
            }
            if (file.size > MAX_PHOTO_BYTES) {
                alert('Размер файла больше 2 МБ');
                input.value = '';
                return;
            }
            preview.src = URL.createObjectURL(file);
            preview.classList.remove('d-none');
            if (icon) {
                icon.classList.add('d-none');
            }
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('[data-theme-toggle]').forEach(function (button) {
            button.addEventListener('click', toggleTheme);
        });
        renderThemeIcons();
        showToasts();
        setupConfirm();
        setupTooltips();
        setupTableFilter();
        setupPhotoInput();
    });
})();
