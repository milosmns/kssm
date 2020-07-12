import me.angrybyte.kssm.impl.action.SingletonAction
import me.angrybyte.kssm.impl.state.SingletonState

// Singleton actions are simple named objects

sealed class WaterAction : SingletonAction() {
  object Heat : WaterAction()
  object Chill : WaterAction()
  object Drink : WaterAction()
  object Fill : WaterAction()
}

// Singleton states are simple named objects

sealed class WaterState : SingletonState() {
  object Ice : WaterState()
  object Steam : WaterState()
  object Liquid : WaterState()
  object Empty : WaterState()
}