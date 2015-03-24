// Placeholder for Actions related to the Context. (E.g., route changes.)

// NOTE: Based on the way external APIs are handled, this is probably a more appropriate point to integrate with the
// router instead of directly coupling it to the ContextStore. Instead have Actions here for changing and getting the
// context, with the certain context changes triggering or being triggered by route changes.

import AppDispatcher from '../dispatcher/appDispatcher';

import ContextEvent from './contextEvents';

const View = {
  Lobby: 'Lobby',
  Room: 'Room'
};

var _context = {
  view: View.Lobby,
  user: null
};

export default new class {
  setCurrentUser(user) {
    AppDispatcher.dispatch({
      type: ContextEvent.UserChanged,
      user: user
    });
  }

  changeView(view) {
    // TODO Set route using the router.
    // When the router changes the route, it will send an event to trigger us to change the view.
  }

  viewChanged(newView) {
    _context.view(view);
    AppDispatcher.dispatch({
      type: ContextEvent.ViewChanged,
      view: _context.view
    });
  }
}
