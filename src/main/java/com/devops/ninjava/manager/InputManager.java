package com.devops.ninjava.manager;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import com.devops.ninjava.manager.ButtonAction;

public class InputManager {

    private final GameEngine engine;

    public InputManager(GameEngine engine) {
        this.engine = engine;
    }

    public void handleKeyPressed(KeyEvent event) {
        KeyCode keyCode = event.getCode();
        GameStatus status = engine.getGameStatus();
        ButtonAction currentAction = ButtonAction.NO_ACTION;

        if (keyCode == KeyCode.UP) {
            if (status == GameStatus.START_SCREEN || status == GameStatus.MAP_SELECTION)
                currentAction = ButtonAction.GO_UP;
            else
                currentAction = ButtonAction.JUMP;
        } else if (keyCode == KeyCode.DOWN) {
            if (status == GameStatus.START_SCREEN || status == GameStatus.MAP_SELECTION)
                currentAction = ButtonAction.GO_DOWN;
            else
                currentAction = ButtonAction.CROUCH;
        } else if (keyCode == KeyCode.RIGHT) {
            currentAction = ButtonAction.M_RIGHT;
        } else if (keyCode == KeyCode.LEFT) {
            currentAction = ButtonAction.M_LEFT;
        } else if (keyCode == KeyCode.ENTER) {
            currentAction = ButtonAction.SELECT;
        } else if (keyCode == KeyCode.ESCAPE) {
            if (status == GameStatus.RUNNING || status == GameStatus.PAUSED)
                currentAction = ButtonAction.PAUSE_RESUME;
            else
                currentAction = ButtonAction.GO_TO_START_SCREEN;
        } else if (keyCode == KeyCode.SPACE) {
            currentAction = ButtonAction.ATTACK;
        }

        notifyInput(currentAction);
    }

    public void handleKeyReleased(KeyEvent event) {
        KeyCode keyCode = event.getCode();
        if (keyCode == KeyCode.RIGHT || keyCode == KeyCode.LEFT) {
            notifyInput(ButtonAction.ACTION_COMPLETED);
        }
    }

    private void notifyInput(ButtonAction action) {
        if (action != ButtonAction.NO_ACTION) {
            engine.receiveInput(action);
        }
    }
}
