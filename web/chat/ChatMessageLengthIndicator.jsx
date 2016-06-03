import React from "react";

export const ChatMessageLengthIndicator = (props) => {

    const classNames = ["input-group-addon",
        ((props.max - props.length < props.highlightAt) ? 'btn-danger' : '')]
        .join(" ");

    return (
        <div className={classNames}>
            {props.max - props.length}
        </div>
    );
}