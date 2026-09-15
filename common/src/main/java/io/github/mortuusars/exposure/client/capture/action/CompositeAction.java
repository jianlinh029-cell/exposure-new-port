package io.github.mortuusars.exposure.client.capture.action;

import java.util.Arrays;
import java.util.List;

public class CompositeAction implements CaptureAction {
    private final List<CaptureAction> actions;

    public CompositeAction(CaptureAction... actions) {
        this.actions = Arrays.stream(actions).filter(action -> !action.equals(CaptureAction.EMPTY)).toList();
    }

    public List<CaptureAction> getActions() {
        return actions;
    }

    @Override
    public int requiredDelayTicks() {
        return actions.stream().mapToInt(CaptureAction::requiredDelayTicks).max().orElse(0);
    }

    @Override
    public void initialize() {
        actions.forEach(CaptureAction::initialize);
    }

    @Override
    public void delayTick(int delayTicksLeft) {
        actions.forEach(component -> component.delayTick(delayTicksLeft));
    }

    @Override
    public void beforeCapture() {
        actions.forEach(CaptureAction::beforeCapture);
    }

    @Override
    public void afterCapture() {
        actions.forEach(CaptureAction::afterCapture);
    }
}
