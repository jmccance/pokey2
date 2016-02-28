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
  },

  allDismissed(identifier) {
    if (identifier === undefined) {
      debug('allDismissed')
    } else {
      debug('allDismissed %s', identifier);
    }

    AppDispatcher.dispatch({
      type: AlertActions.AllDismissed,
      identifier
    });
  }
};

export default AlertActionCreator;
