import React from 'react';
import Glyphicon from 'react-bootstrap/lib/Glyphicon';
import Nav from 'react-bootstrap/lib/Nav';
import Navbar from 'react-bootstrap/lib/Navbar';
import NavItem from 'react-bootstrap/lib/NavItem';

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
