'use strict';

pinpointApp.factory('filteredMapUtil', [ 'filterConfig',
    function (cfg) {
        // define private variables
        var self;

        return {

            /**
             * parse filter
             * @param filterDataSet
             */
            parseFilter: function (filterDataSet, application, prevFilter) {
                if (filterDataSet.srcApplicationName === 'USER') {
                    application = filterDataSet.destApplicationName + '@1010';
                } else {
                    application = filterDataSet.srcApplicationName + '@1010';
                }

                var newFilter = ((prevFilter) ? prevFilter + cfg.FILTER_DELIMETER : "")
                    + filterDataSet.srcServiceType + cfg.FILTER_ENTRY_DELIMETER
                    + filterDataSet.srcApplicationName + cfg.FILTER_ENTRY_DELIMETER
                    + filterDataSet.destServiceType + cfg.FILTER_ENTRY_DELIMETER
                    + filterDataSet.destApplicationName;

                if (angular.isString(filterDataSet.label)) {
                    if (filterDataSet.label === 'error') {
                        newFilter += cfg.FILTER_ENTRY_DELIMETER + filterDataSet.label;
                    } else if (filterDataSet.label.indexOf('+') > 0) {
                        newFilter += cfg.FILTER_ENTRY_DELIMETER + parseInt(filterDataSet.label, 10) + ',9999999999';
                    } else {
                        var startValue = self.getStartValueForFilterByLabel(filterDataSet.label, filterDataSet.values);
                        newFilter += cfg.FILTER_ENTRY_DELIMETER + startValue + ',' + filterDataSet.label;
                    }

                }
                return newFilter;
            },

            /**
             * get start value for filter by label
             * @param label
             * @param values
             * @returns {number}
             */
            getStartValueForFilterByLabel: function (label, values) {
                var labelKey = (function () {
                        for (var key in values) {
                            if (values[key].label === label) {
                                return key;
                            }
                        }
                        return false;
                    })(),
                    startValue = 0;

                if (labelKey > 0) {
                    startValue = parseInt(values[labelKey - 1].label, 10);
                }
                return startValue;
            }
        }
    }
]);
