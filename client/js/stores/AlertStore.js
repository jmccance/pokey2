import EventEmitter from 'events';
import { List } from 'immutable';

import AlertActions from '../actions/AlertActions';
import AppDispatcher from '../dispatcher/appDispatcher';
import Debug from '../util/Debug';

const debug = Debug('stores:AlertStore');

const InternalEvents = {
  Change: 'CHANGE'
};

class AlertStore extends EventEmitter {
  constructor() {
    super();

    this._alerts = List(['panic', 'plague', 'disco']);

    AppDispatcher.register((action) => {
      switch (action.type) {
        case AlertActions.AlertDismissed:
          debug('alert_dismissed %i', action.index);
          this._alerts = this._alerts.remove(action.index);
          this._emitChange();
          break;

        default:
          // do nothing
      }
    });
  }

  addChangeListener(callback) {
    this.addListener(InternalEvents.Change, callback);
  }

  removeChangeListener(callback) {
    this.removeListener(InternalEvents.Change, callback);
  }

  getAlerts() {
    return this._alerts;
  }

  /**
   * @private
   */
  _emitChange() {
    this.emit(InternalEvents.Change);
  }
}

export default new AlertStore();
