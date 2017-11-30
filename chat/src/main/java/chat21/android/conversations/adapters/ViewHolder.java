package chat21.android.conversations.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.vanniktech.emoji.EmojiTextView;

import chat21.android.R;
import chat21.android.adapters.AbstractRecyclerAdapter;
import chat21.android.conversations.models.Conversation;
import chat21.android.core.ChatManager;
import chat21.android.user.models.IChatUser;
import chat21.android.utils.StringUtils;
import chat21.android.utils.TimeUtils;
import chat21.android.utils.glide.CropCircleTransformation;

/**
 * Created by stefanodp91 on 19/10/17.
 */

public class ViewHolder extends RecyclerView.ViewHolder {
    private final TextView mRecipientName;
    private final EmojiTextView mLastReceivedMessage;
    private final TextView mTimestamp;
    private final ImageView mProfilePicture;

    private static final String TAG = ViewHolder.class.getName();

    public ViewHolder(View itemView) {
        super(itemView);
        mRecipientName = (TextView) itemView.findViewById(R.id.recipient_name);
        mLastReceivedMessage = (EmojiTextView) itemView.findViewById(R.id.last_received_message);
        mTimestamp = (TextView) itemView.findViewById(R.id.timestamp);
        mProfilePicture = (ImageView) itemView.findViewById(R.id.profile_picture);
    }

    public void bind(
            Conversation conversation,
            int position,
            AbstractRecyclerAdapter.OnRecyclerItemClickListener<Conversation> onRecyclerItemClickListener,
            AbstractRecyclerAdapter.OnRecyclerItemLongClickListener<Conversation> onRecyclerItemLongClickListener) {

        // set text style bold
        try {
            setLastMessageText(conversation);
        } catch (Exception e) {
            Log.e(TAG, "ViewHolder.bind: cannot set the last message text. " + e.getMessage());
        }

        try {
            setTimestamp(conversation);
        } catch (Exception e) {
            Log.e(TAG, "ViewHolder.bind: cannot set the timestamp. " + e.getMessage());
        }

        try {
            if (StringUtils.isValid(conversation.getGroup_id())) {
                try {
                    // it is a group
                    setGroupInfo(conversation);
                } catch (Exception e) {
                    Log.e(TAG, "ViewHolder.bind: cannot set the group info. " + e.getMessage());
                }
            } else {
                try {
                    // it is a one to one conversation
                    // set user name and profile picture
                    setUserInfo(conversation);
                } catch (Exception e) {
                    Log.e(TAG, "ViewHolder.bind: cannot set the user info. " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "ViewHolder.bind: cannot set the conversation group id. " + e.getMessage());
        }

        onViewClickListener(conversation, position, onRecyclerItemClickListener);

        onViewLongClickListener(conversation, position, onRecyclerItemLongClickListener);
    }

    private void setTimestamp(Conversation conversation) throws Exception {
        String timestamp = TimeUtils.getFormattedTimestamp(conversation.getTimestampLong());
        mTimestamp.setText(timestamp);
    }

    // Issue #21
    private void setLastMessageText(Conversation conversation)
            throws Exception {
        String text = conversation.getLast_message_text();

        //  group conversation
        if (StringUtils.isValid(conversation.getGroup_id())) {
            IChatUser loggedUser = ChatManager.getInstance().getLoggedUser(); // retrieve the lgoged user
            String loggedUserId = loggedUser.getId(); // retrive the logged user id

            String senderId = conversation.getSender(); // retrieve the senderId
            // retrieve the sender fullname
            String senderFullname = conversation.getSender_fullname();

            // the currrent user is not the sender of the message
            if (!loggedUserId.equals(senderId)) {
                // sho the sender display name
                text = StringUtils.isValid(senderFullname) ? senderFullname + ": " + text
                        : senderId + ": " + text;
            }
        }

        // show the message bold if is a new message
        // otherwise show a normal message
        boolean hasNewMessage = conversation.getIs_new();
        if (hasNewMessage) {
            // bold
            showLastMessageTextBold(text);
        } else {
            // not bold
            mLastReceivedMessage.setText(text);
        }
    }

    private void showLastMessageTextBold(String string) {
        mLastReceivedMessage.setText(Html.fromHtml("<b>" + string + "</b>"));
    }


    private void setGroupInfo(Conversation conversation) throws Exception {
        mRecipientName.setText(StringUtils.isValid(conversation.getGroup_name()) ?
                conversation.getGroup_name() : conversation.getGroup_id());
        setGroupPicture();
    }

    private void setGroupPicture() {

        Glide
                .with(itemView.getContext())
                .load("")
                .placeholder(R.drawable.ic_group_avatar)
                .bitmapTransform(new CropCircleTransformation(itemView.getContext()))
                .into(mProfilePicture);
    }


    private void setUserInfo(Conversation conversation) throws Exception {
        String denormalizedConversWithId = conversation.getConvers_with().replace("_", ".");

        //first of all set display name to username. if fullname is not found show username
        // set username
        String displayName = StringUtils.isValid(conversation.getConvers_with_fullname()) ?
                conversation.getConvers_with_fullname() : denormalizedConversWithId;

        mRecipientName.setText(displayName);

        setProfilePicture(""); // TODO: 19/10/17
    }

    private void setProfilePicture(String url) {

        Glide
                .with(itemView.getContext())
                .load("")
                .placeholder(R.drawable.ic_person_avatar)
                .bitmapTransform(new CropCircleTransformation(itemView.getContext()))
                .into(mProfilePicture);
    }

    // When click on a conversation open the related list of messages.
    private void onViewClickListener(
            final Conversation conversation,
            final int position,
            final AbstractRecyclerAdapter.OnRecyclerItemClickListener<Conversation> callback) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (callback != null) {
                    callback.onRecyclerItemClicked(conversation, position);
                } else {
                    Log.e(TAG, "ViewHolder.onViewClickListener: onRecyclerItemClickListener is null. " +
                            "set it with addOnRecyclerItemClickListener method. ");
                }
            }
        });
    }

    // When long click on a conversation
    private void onViewLongClickListener(
            final Conversation conversation,
            final int position,
            final AbstractRecyclerAdapter.OnRecyclerItemLongClickListener<Conversation> callback) {
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (callback != null) {
                    callback.onRecyclerItemLongClicked(conversation, position);

                    // source :
                    // https://stackoverflow.com/questions/18911290/perform-both-the-normal-click-and-long-click-at-button
                    return true; // event triggered
                } else {
                    Log.e(TAG, "ViewHolder.onViewLongClickListener: mOnRecyclerItemLongClickListener is null. " +
                            "set it with addOnRecyclerItemLongClickListener method. ");
                }

                return false; // event not triggered
            }
        });
    }
}