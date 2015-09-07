import AppDispatcher from '../dispatcher/appDispatcher';
import Debug from '../util/Debug';
import AlertActions from './AlertActions';

const debug = Debug('alerts:AlertActionCreator');

const AlertActionCreator = {
  alertCreated(alert) {
    debug('alertCreated %o', alert);
    AppDispatcher.dispatch({
      type: AlertActions.AlertCreated,
      alert
    });
  },

  alertDismissed(index) {
    debug('alertDismissed %o', index);
    AppDispatcher.dispatch({
      type: AlertActions.AlertDismissed,
      index
    });
  }
};

export default AlertActionCreator;
