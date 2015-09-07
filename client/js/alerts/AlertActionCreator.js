import AppDispatcher from '../dispatcher/appDispatcher';
import Debug from '../util/Debug';
import AlertActions from './AlertActions';

const debug = Debug('alerts:AlertActionCreator');

const AlertActionCreator = {
  alertDismissed(index) {
    debug('alertDismissed %o', index);
    AppDispatcher.dispatch({
      type: AlertActions.AlertDismissed,
      index
    });
  }
}

export default AlertActionCreator;
