// Placeholder for Actions related to the Context. (E.g., route changes.)

// NOTE: Based on the way external APIs are handled, this is probably a more appropriate point to integrate with the
// router instead of directly coupling it to the ContextStore. Instead have Actions here for changing and getting the
// context, with the certain context changes triggering or being triggered by route changes.

import AppDispatcher from '../dispatcher/appDispatcher';

import ContextEvent from './contextEvents';

export default new class {
  setCurrentUser(user) {
    AppDispatcher.dispatch({
      type: ContextEvent.UserChanged,
      user: user
    });
  }

  viewChanged(newView) {
    AppDispatcher.dispatch({
      type: ContextEvent.ViewChanged,
      view: newView
    });
  }
}
