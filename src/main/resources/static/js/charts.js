(function () {
    var SERIES_COUNT = 6;
    var instances = [];

    function cssVariable(name) {
        return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
    }

    function palette() {
        var series = [];
        for (var i = 1; i <= SERIES_COUNT; i++) {
            series.push(cssVariable('--chart-' + i));
        }
        return {
            series: series,
            text: cssVariable('--bs-secondary-color'),
            grid: cssVariable('--app-border'),
            surface: cssVariable('--app-surface')
        };
    }

    function buildConfig(spec, colors) {
        var datasets = spec.datasets.map(function (dataset, index) {
            return {
                label: dataset.label,
                data: dataset.data,
                backgroundColor: colors.series[index % SERIES_COUNT],
                borderColor: colors.series[index % SERIES_COUNT],
                borderRadius: 6,
                maxBarThickness: 48
            };
        });
        return {
            type: spec.type,
            data: {labels: spec.labels, datasets: datasets},
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {mode: 'index', intersect: false},
                plugins: {
                    legend: {labels: {color: colors.text, usePointStyle: true, boxWidth: 8}},
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                var value = context.parsed.y;
                                return value === null ? null : context.dataset.label + ': ' + value + ' м';
                            }
                        }
                    }
                },
                scales: {
                    x: {ticks: {color: colors.text}, grid: {display: false}},
                    y: {
                        beginAtZero: true,
                        ticks: {color: colors.text},
                        grid: {color: colors.grid},
                        title: {display: Boolean(spec.yLabel), text: spec.yLabel, color: colors.text}
                    }
                }
            }
        };
    }

    function destroyAll() {
        instances.forEach(function (instance) {
            instance.destroy();
        });
        instances = [];
    }

    function renderAll() {
        destroyAll();
        var colors = palette();
        document.querySelectorAll('canvas[data-chart]').forEach(function (canvas) {
            var spec = JSON.parse(canvas.getAttribute('data-chart'));
            instances.push(new Chart(canvas, buildConfig(spec, colors)));
        });
    }

    document.addEventListener('DOMContentLoaded', renderAll);
    document.addEventListener('themechange', renderAll);
})();
