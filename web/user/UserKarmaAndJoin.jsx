import React, { Component } from 'react';
import Immutable from 'immutable';
import moment from 'moment';

export default class UserKarma extends Component {
  renderKarma() {
    const { user } = this.props;
    const commentKarma = user.get('commentKarma', 0) - 0;
    const linkKarma = user.get('linkKarma', 0) - 0;
    const totalKarma = commentKarma + linkKarma;
    if (isNaN(totalKarma) || !totalKarma) {
      return null;
    }

    const totalKarmaString = totalKarma.toLocaleString();
    return <span className="karma" title={`${totalKarmaString} total reddit karma`}>{totalKarmaString}</span>;
  }
  renderJoinDate() {
    const { user } = this.props;

    const joinDateUTC = user.get('redditUserCreatedUTC', 0) - 0;
    if (!joinDateUTC || joinDateUTC < 0) {
      return null;
    }

    const joinDate = new Date(joinDateUTC * 1000);

    return <span className="since text-muted" title={`Joined reddit ${moment(joinDate).format('MM/DD/YYYY')}`}>since {moment(joinDate).format('MM/YYYY')}</span>;
  }
  render() {
    return (
      <div style={{ fontSize: '0.8em', cursor: 'default' }}>
        {this.renderKarma()}
        {this.renderJoinDate()}
      </div>
    );
  }
}

UserKarma.defaultProps = {
  user: Immutable.Map()
};

