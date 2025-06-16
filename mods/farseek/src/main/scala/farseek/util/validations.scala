package farseek.util

private val DefaultDesc = "value"

extension[T](x: into T)
  def assert(p: T => Boolean, mustBe: => String, xDesc: => String = DefaultDesc): Unit =
    Predef.assert (p(x), s"$xDesc is ${x.toString}, must be $mustBe")
  def require(p: T => Boolean, mustBe: => String, xDesc: => String = DefaultDesc): Unit =
    Predef.require(p(x), s"$xDesc is ${x.toString}, must be $mustBe")

  def asserted(p: T => Boolean, mustBe: => String, xDesc: => String = DefaultDesc): T =
    x.tap(_.assert (p, mustBe, xDesc))
  def required(p: T => Boolean, mustBe: => String, xDesc: => String = DefaultDesc): T =
    x.tap(_.require(p, mustBe, xDesc))
end extension

extension[T](p: into (T => Boolean))
  def validate (x: T, mustBe: => String, xDesc: => String): Unit = x.require (p, mustBe, xDesc)
  def validated(x: T, mustBe: => String, xDesc: => String):    T = x.required(p, mustBe, xDesc)

extension[T](pf: PartialConversion[T, ?])
  def validate (x: T, xDesc: => String = DefaultDesc): Unit = x.require (pf.isDefinedAt, pf.mustBe, xDesc)
  def validated(x: T, xDesc: => String = DefaultDesc):    T = x.required(pf.isDefinedAt, pf.mustBe, xDesc)
