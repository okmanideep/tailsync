# SyncTileService Test Cases

A comprehensive list of test scenarios for verifying the functionality of `SyncTileService`. These can be implemented using Robolectric + Mockito (or your preferred Android test framework).

---

## 1. onClick() Behavior

1. **Start flow**
   - Precondition:
     - `qsTile.state == Tile.STATE_INACTIVE`
     - `syncthingService == null`
   - Action: `service.onClick()`
   - Expectations:
     - Broadcast `com.nutomic.syncthingandroid.action.START` is sent
     - `bindService(...)` is invoked with `SyncthingService` intent
     - `qsTile.state` becomes `STATE_ACTIVE`
     - On API ≥ Q: `qsTile.subtitle == "Starting"`
     - `qsTile.updateTile()` is called

2. **Stop flow**
   - Precondition:
     - `qsTile.state == Tile.STATE_ACTIVE`
     - `syncthingService` is non-null and bound
   - Action: `service.onClick()`
   - Expectations:
     - Broadcast `com.nutomic.syncthingandroid.action.STOP` is sent
     - `updateTileAsInactive()` sets `qsTile.state = STATE_INACTIVE`, clears subtitle, calls `updateTile()`
     - `syncthingService.unregisterOnServiceStateChangeListener(this)` is called
     - `unbindService(this)` is called
     - `syncthingService` is set to null

3. **Ignored click**
   - Precondition:
     - `qsTile.state == Tile.STATE_ACTIVE`
     - `syncthingService == null`
   - Action: `service.onClick()`
   - Expectations:
     - No broadcasts sent
     - No bind/unbind calls
     - `qsTile.state` remains unchanged

---

## 2. Listening Lifecycle

4. **onStartListening – binds when service is null**
   - Precondition: `syncthingService == null`
   - Action: `service.onStartListening()`
   - Expectations:
     - `isListening == true`
     - `bindService(...)` is invoked

5. **onStartListening – skips bind if already bound**
   - Precondition: `syncthingService` is non-null
   - Action: `service.onStartListening()`
   - Expectations:
     - `isListening == true`
     - No additional `bindService(...)` calls

6. **onStopListening – no-op when service is null**
   - Precondition: `syncthingService == null`
   - Action: `service.onStopListening()`
   - Expectations:
     - `isListening == false`
     - No `unbindService(...)` or listener changes

7. **onStopListening – teardown when service present**
   - Precondition: `syncthingService` is non-null and listener registered
   - Action: `service.onStopListening()`
   - Expectations:
     - `isListening == false`
     - `syncthingService.unregisterOnServiceStateChangeListener(this)` is called
     - `unbindService(this)` is called
     - `syncthingService` is set to null

---

## 3. ServiceConnection Callbacks

8. **onServiceConnected – ACTIVE state → updateActiveTile**
   - Setup:
     - Mock `SyncthingServiceBinder` whose service has `currentState = ACTIVE`
     - Stub `service.api.getOverallCompletionInfo(...)` to callback with `completion = 0.5`
   - Action: `onServiceConnected(component, binder)`
   - Expectations:
     - `service.registerOnServiceStateChangeListener(this)` called
     - `syncthingService` set to binder’s service
     - Inside `updateActiveTile`:
       - `qsTile.state == STATE_ACTIVE`
       - On API ≥ Q: `qsTile.subtitle == "Syncing 50%"`
       - `qsTile.updateTile()` called

9. **onServiceConnected – ACTIVE idle subtitle**
   - Same as #8 but stub `completion = 1.0`
   - Expectation: `qsTile.subtitle == "Idle"`

10. **onServiceConnected – DISABLED state → updateTileAsInactive**
    - Setup: service `currentState = DISABLED`
    - Action: `onServiceConnected(...)`
    - Expectations:
      - `qsTile.state == STATE_INACTIVE`
      - Subtitle is cleared
      - `qsTile.updateTile()` called

11. **onServiceConnected – ERROR state → updateTileAsUnavailable**
    - Setup: service `currentState = ERROR`
    - Action: `onServiceConnected(...)`
    - Expectations:
      - `qsTile.state == STATE_UNAVAILABLE`
      - Subtitle is cleared
      - `qsTile.updateTile()` called

12. **onServiceDisconnected**
    - Precondition: `syncthingService` is non-null
    - Action: `onServiceDisconnected(...)`
    - Expectation: `syncthingService` set to null

13. **onNullBinding**
    - Action: `onNullBinding(...)`
    - Expectation: no exceptions thrown

---

## 4. Tile-Update Helpers

14. **updateActiveTile – missing API**
    - Precondition: `syncthingService.api == null`
    - Action: `updateActiveTile()`
    - Expectation: no crash, no `updateTile()` calls

15. **updateActiveTile – missing tile**
    - Precondition: `qsTile == null`
    - Action: `updateActiveTile()`
    - Expectation: no crash

16. **updateTileAsInactive**
    - Action: `updateTileAsInactive()`
    - Expectations:
      - `qsTile.state == STATE_INACTIVE`
      - Subtitle cleared on API ≥ Q
      - `qsTile.updateTile()` called

17. **updateTileAsUnavailable**
    - Action: `updateTileAsUnavailable()`
    - Expectations:
      - `qsTile.state == STATE_UNAVAILABLE`
      - Subtitle cleared on API ≥ Q
      - `qsTile.updateTile()` called

---

## 5. Edge & Error Cases

18. **Rapid toggles**
    - Simulate quick successive `onClick()` calls; ensure no double-bind or leaks

19. **API callback errors/timeouts**
    - Stub `getOverallCompletionInfo()` to throw or never callback; verify no UI crashes

20. **Version guard**
    - Run tests under SDK < Q to ensure subtitle logic is skipped but tile states still update
