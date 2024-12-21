package de.byteingpython.sshGame.screen;

import de.byteingpython.sshGame.event.EventListener;
import de.byteingpython.sshGame.friends.FriendManager;
import de.byteingpython.sshGame.friends.FriendRequest;
import de.byteingpython.sshGame.friends.FriendUpdateEvent;
import de.byteingpython.sshGame.friends.FriendUpdateEventType;
import de.byteingpython.sshGame.lobby.Lobby;
import de.byteingpython.sshGame.player.Player;
import de.byteingpython.sshGame.player.PlayerManager;
import de.byteingpython.sshGame.utils.Message;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class FriendMenuScreen {
    private final FriendManager friendManager;
    private final PlayerManager playerManager;
    private final Player player;
    private final SelectScreen<String> stringSelectScreen;
    private final SelectScreen<FriendRequest> friendRequestSelectScreen;
    private FriendMenuStage friendMenuStage = FriendMenuStage.FRIEND_LIST;
    private Optional<String> selectedFriend = Optional.empty();
    private Optional<FriendRequest> selectedFriendRequest = Optional.empty();

    public FriendMenuScreen(Player player, FriendManager friendManager, PlayerManager playerManager) {
        this.friendManager = friendManager;
        this.player = player;
        stringSelectScreen = new SelectScreen<>(player);
        friendRequestSelectScreen = new SelectScreen<>(player);
        this.playerManager = playerManager;
    }

    @EventListener
    public void onUpdate(FriendUpdateEvent event) {
        if (friendMenuStage.equals(FriendMenuStage.FRIEND_LIST) && event.getType().equals(FriendUpdateEventType.ADDED)) {
            updateFriendSelectScreen();
        }
        if (friendMenuStage.equals(FriendMenuStage.FRIEND_LIST) && event.getType().equals(FriendUpdateEventType.REMOVED)) {
            stringSelectScreen.removeOption(event.getFriendName());
        }
        if (friendMenuStage.equals(FriendMenuStage.FRIEND_LIST) && event.getType().equals(FriendUpdateEventType.REQUESTED)) {
            updateFriendSelectScreen();
        }
        if (friendMenuStage.equals(FriendMenuStage.REQUEST_LIST) && event.getType().equals(FriendUpdateEventType.ADDED)) {
            updateFriendRequestScreen();
        }
        if (friendMenuStage.equals(FriendMenuStage.FRIEND_OPTIONS) && event.getType().equals(FriendUpdateEventType.REMOVED)) {
            if (selectedFriend.isEmpty()) {
                return;
            }
            if (selectedFriend.get().equals(event.getFriendName())) {
                stringSelectScreen.clearOptions();
            }
        }
        if (friendMenuStage.equals(FriendMenuStage.REQUEST_OPTIONS) && event.getType().equals(FriendUpdateEventType.ADDED)) {
            if (selectedFriendRequest.isEmpty()) {
                return;
            }
            if (selectedFriendRequest.get().getSource().equals(event.getFriendName())) {
                friendRequestSelectScreen.clearOptions();
            }
        }
    }

    public void updateFriendSelectScreen() {
        List<String> friends = friendManager.getFriends(player);
        List<FriendRequest> friendRequests = friendManager.getFriendRequests(player);
        stringSelectScreen.clearOptions();
        if (!friendRequests.isEmpty()) {
            stringSelectScreen.addOption(player.getLocale().getString("friend_requests") + " (" + friendRequests.size() + ")", "-1");
        }
        for (String friend : friends) {
            if (playerManager.getPlayer(friend).isPresent()) {
                stringSelectScreen.addOption(friend + " - " + player.getLocale().getString("online"), friend);
            } else {
                stringSelectScreen.addOption(friend, friend);
            }
        }
        stringSelectScreen.addOption(player.getLocale().getString("add_friend"), "");
    }

    private void updateFriendRequestScreen() {
        List<FriendRequest> friendRequests = friendManager.getFriendRequests(player);
        friendRequestSelectScreen.clearOptions();
        for (FriendRequest friendRequest : friendRequests) {
            friendRequestSelectScreen.addOption(friendRequest.getSource(), friendRequest);
        }
    }

    public void showFriendSelectScreen() {
        friendMenuStage = FriendMenuStage.FRIEND_LIST;
        updateFriendSelectScreen();
        CountDownLatch latch = new CountDownLatch(1);
        stringSelectScreen.selectOption(player.getLocale().getString("friend_list"), latch::countDown);
        try {
            latch.await();
        } catch (InterruptedException e) {
            //TODO Handle Errors properly
            throw new RuntimeException(e);
        }
        if (stringSelectScreen.getSelected().isEmpty()) {
            return;
        }
        if (stringSelectScreen.getSelected().get().isEmpty()) {
            try {
                CountDownLatch textLatch = new CountDownLatch(1);
                TextInputScreen textInputScreen = new TextInputScreen(textLatch::countDown, player, player.getLocale().getString("friend_invite_input_title"));
                textLatch.await();
                if (textInputScreen.getInput().isEmpty()) {
                    showFriendSelectScreen();
                }
                String friendName = textInputScreen.getInput();
                if (friendName.equals(player.getName())) {
                    player.getEventHandler().handle(new LobbyScreenMessageEvent(new Message(player.getLocale().getString("friend_request_self"), 2000)));
                    return;
                }
                friendManager.createFriendRequest(player, friendName);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        if (stringSelectScreen.getSelected().get().equals("-1")) {
            showFriendRequestsScreen();
            return;
        }
        showFriendOptionsScreen(stringSelectScreen.getSelected().get());
    }

    public void showFriendRequestsScreen() {
        friendMenuStage = FriendMenuStage.REQUEST_LIST;
        updateFriendRequestScreen();
        CountDownLatch latch = new CountDownLatch(1);
        friendRequestSelectScreen.selectOption(player.getLocale().getString("friend_requests"), latch::countDown);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (friendRequestSelectScreen.getSelected().isEmpty()) {
            showFriendSelectScreen();
        }
        showFriendRequestOptionsScreen(friendRequestSelectScreen.getSelected().get());
    }

    private void showFriendOptionsScreen(String friend) {
        friendMenuStage = FriendMenuStage.FRIEND_OPTIONS;
        this.selectedFriend = Optional.of(friend);
        this.stringSelectScreen.clearOptions();
        if (playerManager.getPlayer(friend).isPresent()) {
            stringSelectScreen.addOption(player.getLocale().getString("join"), "join");
        }
        stringSelectScreen.addOption(player.getLocale().getString("remove"), "remove");
        CountDownLatch latch = new CountDownLatch(1);
        stringSelectScreen.selectOption(friend, latch::countDown);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (stringSelectScreen.getSelected().isEmpty()) {
            showFriendSelectScreen();
        }
        if (stringSelectScreen.getSelected().get().equals("join")) {
            Optional<Player> friendPlayer = playerManager.getPlayer(friend);
            if (friendPlayer.isEmpty()) {
                showFriendSelectScreen();
            }
            Lobby lobby = friendPlayer.get().getLobby();
            if (lobby.isPlaying()) {
                showFriendSelectScreen();
            }
            if (lobby.getPlayers().size() >= lobby.getGame().getMaxLobbySize()) {
                showFriendSelectScreen();
            }
            lobby.addPlayer(player);
            for (Player lobbyPlayer : lobby.getPlayers()) {
                lobbyPlayer.getEventHandler().handle(new ScreenUpdateEvent());
            }
            return;
        }
        if (stringSelectScreen.getSelected().get().equals("remove")) {
            friendManager.removeFriend(player, friend);
            showFriendSelectScreen();
        }
    }

    private void showFriendRequestOptionsScreen(FriendRequest friendRequest) {
        this.friendMenuStage = FriendMenuStage.REQUEST_OPTIONS;
        this.selectedFriendRequest = Optional.of(friendRequest);
        this.stringSelectScreen.clearOptions();
        stringSelectScreen.addOption(player.getLocale().getString("accept"), "accept");
        stringSelectScreen.addOption(player.getLocale().getString("reject"), "reject");
        CountDownLatch latch = new CountDownLatch(1);
        stringSelectScreen.selectOption(friendRequest.getSource(), latch::countDown);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (stringSelectScreen.getSelected().isEmpty()) {
            showFriendRequestsScreen();
        }
        if (stringSelectScreen.getSelected().get().equals("accept")) {
            friendRequest.accept();
        }
        if (stringSelectScreen.getSelected().get().equals("reject")) {
            friendRequest.decline();
        }
    }

    public void show(Runnable runnable) {
        player.getEventHandler().registerListeners(this);
        new Thread(() -> {
            showFriendSelectScreen();
            player.getEventHandler().unregisterListeners(this);
            runnable.run();
        }).start();
    }

}
