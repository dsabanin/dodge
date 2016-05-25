package skullbeware

/**
  * Created by dsabanin on 5/9/16.
  */
class Stats {
  val attrs =
    new scala.collection.mutable.HashMap[String, Int].withDefaultValue(0)

  def incr(key: String) = {
    attrs(key) += 1
  }

  def get(key: String) = attrs(key)

  def foreach(f: ((String, Int)) => Unit): Unit = attrs.foreach(f)

  def reset() = {
    attrs.clear()
  }
}
