import { Record } from 'immutable';

const Alert = Record({
  level: '',
  identifier: '',
  message: ''
});

const Alerts = {
  danger: (msg, identifier) => new Alert({
    level: 'danger',
    identifier: identifier,
    message: msg
  }),

  success: (msg, identifier) => new Alert({
    level: 'success',
    identifier: identifier,
    message: msg
  }),

  info: (msg, identifier) => new Alert({
    level: 'info',
    identifier: identifier,
    message: msg
  })
};

export default Alerts;
