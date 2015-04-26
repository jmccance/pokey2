import React from 'react';
import ModalTrigger from 'react-bootstrap/lib/ModalTrigger';
import Navbar from 'react-bootstrap/lib/Navbar';
import Nav from 'react-bootstrap/lib/Nav';
import NavItem from 'react-bootstrap/lib/NavItem';

import EditProfileModal from './EditProfileModal';

export default class extends React.Component {
  render() {
    return (
      <Navbar brand='Pokey' className='staticTop'>
        <Nav right>
          <ModalTrigger modal={<EditProfileModal />}>
            <NavItem>
              Edit Profile
            </NavItem>
          </ModalTrigger>
        </Nav>
      </Navbar>
    );
  }
}