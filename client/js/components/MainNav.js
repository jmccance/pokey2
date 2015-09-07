import React from 'react';
import {Glyphicon, Nav, Navbar, NavItem} from 'react-bootstrap';

import EditProfileModal from './EditProfileModal';

class MainNav extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showEditProfileModal: false
    };

    this.openEditProfileModal = () => this.setState({ showEditProfileModal: true });
    this.closeEditProfileModal = () => this.setState({ showEditProfileModal: false });
  }

  render() {
    let nameLabel;
    if (this.props.user) {
      nameLabel = this.props.user.name;
    } else {
      nameLabel = 'Click To Set Your Name';
    }

    return (
      <Navbar brand='Pokey' className='staticTop'>
        <Nav>
          <NavItem href='#/'>Lobby</NavItem>
        </Nav>
        <Nav right>
          <NavItem>
            <span onClick={this.openEditProfileModal}>
              {nameLabel} <Glyphicon glyph='pencil' />
              <EditProfileModal
                show={this.state.showEditProfileModal}
                onHide={this.closeEditProfileModal}
                close={this.closeEditProfileModal} />
            </span>
          </NavItem>
        </Nav>
      </Navbar>
    );
  }
}

export default MainNav;
