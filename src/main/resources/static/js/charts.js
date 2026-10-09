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
        var isLine = spec.type === 'line';
        var datasets = spec.datasets.map(function (dataset, index) {
            var color = colors.series[index % SERIES_COUNT];
            var base = {label: dataset.label, data: dataset.data, backgroundColor: color, borderColor: color};
            if (isLine) {
                return Object.assign(base, {tension: 0.25, spanGaps: true, borderWidth: 2, pointRadius: 3, borderDash: dataset.dashed ? [6, 4] : []});
            }
            return Object.assign(base, {borderRadius: 6, maxBarThickness: 48});
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
                                var unit = spec.unit === undefined ? 'м' : spec.unit;
                                return value === null ? null : context.dataset.label + ': ' + value + (unit ? ' ' + unit : '');
                            }
                        }
                    }
                },
                scales: {
                    x: {ticks: {color: colors.text}, grid: {display: false}},
                    y: {
                        beginAtZero: spec.type !== 'line',
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
