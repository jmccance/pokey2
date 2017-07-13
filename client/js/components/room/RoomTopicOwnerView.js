import React from "react";
import {PageHeader} from "react-bootstrap";

import PokeyActionCreator from "../../pokey/PokeyActionCreator";

const ESCAPE_KEY = 27;
const ENTER_KEY = 13;

export default class extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      editedTopic: this.props.topic
    };

    this.handleChange = (event) => {
      this.setState({editedTopic: event.target.value});
    };

    this.handleKeyDown = (event) => {
      switch (event.which) {
        case ENTER_KEY:
          this.handleSubmit(event);
          break;

        case ESCAPE_KEY:
          this.setState({editedTopic: this.props.topic});
          break;

        default:
          // Nothing
      }
    };

    this.handleSubmit = () => {
      const newTopic = this.state.editedTopic.trim();
      PokeyActionCreator.topicSet(props.roomId, newTopic);
    };
  }

  render() {
    return (
      <PageHeader>
        <input
          id='topic-input'
          value={this.state.editedTopic}
          placeholder='Click to set a topic for estimation'
          onBlur={this.handleSubmit}
          onChange={this.handleChange}
          onKeyDown={this.handleKeyDown}
        />
      </PageHeader>
    );
  }
}
