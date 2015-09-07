import EventEmitter from 'events';
import { List } from 'immutable';

import AppDispatcher from '../dispatcher/appDispatcher';
import Debug from '../util/Debug';
import AlertActions from './AlertActions';

const debug = Debug('alerts:AlertStore');

const InternalEvents = {
  Change: 'CHANGE'
};

class AlertStore extends EventEmitter {
  constructor() {
    super();

    this._alerts = List();

    AppDispatcher.register((action) => {
      switch (action.type) {
        case AlertActions.AlertCreated:
          debug('alert_created %o', action.alert);
          this._alerts = this._alerts.push(action.alert);
          this._emitChange();
          break;

        case AlertActions.AlertDismissed:
          debug('alert_dismissed %i', action.index);
          this._alerts = this._alerts.remove(action.index);
          this._emitChange();
          break;

        default:
          debug('unhandled_event %o', action);
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
