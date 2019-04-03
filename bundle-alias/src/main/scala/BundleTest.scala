// See README.md for license details.
import chisel3._

/**
  * AIf
  */
class AIf extends Bundle {
  val a = Input(Bool())
  val aa = Output(UInt(32.W))
}

/**
  * BIf
  */
class BIf extends Bundle {
  val b = Input(Bool())
  val bb = Output(UInt(32.W))
}

/**
  * CIf - BIfを継承しているのでb/bb/c/ccが変数として存在している
  */
class CIf extends BIf {
  val c = Input(Bool())
  val cc = Output(UInt(32.W))
}

/**
  * ABIf
  * @param useBIf BIf をインスタンスするかどうか
  */
class ABIf(useBIf: Boolean) extends Bundle {
  val a = new AIf
  val b = if (useBIf) Some(new BIf) else None

  override def cloneType: ABIf.this.type = new ABIf(useBIf).asInstanceOf[this.type]
}

/**
  * ABCIf
  * @param useBIf BIf をインスタンスするかどうか
  */
class ABCIf(useBIf: Boolean) extends Bundle {
  val a = new AIf
  val b = if (useBIf) Some(new BIf) else None
  val c = new CIf

  override def cloneType: ABCIf.this.type = new ABCIf(useBIf).asInstanceOf[this.type]
}

/**
  * まずは別の変数に移して、設定が出来るかどうかを確認
  * @param useBIf BIfを使用するかどうか
  */
class BundleTestModule1(useBIf: Boolean) extends Module {
  val io = IO(new ABIf(useBIf))

  val a = io.a

  when (a.a) {
    a.aa := 0x12345678.U
  } .otherwise {
    a.aa := 0x87654321L.U
  }

  if (useBIf) {
    val b = io.b.get
    when (b.b) {
      b.bb := 0x12345678.U
    } .otherwise {
      b.bb := 0x87654321L.U
    }
  }
}

/**
  * 設定用のメソッドを用意して、メソッド内で値を設定してみる
  * @param useBIf BIfを使用するかどうか
  */
class BundleTestModule2(useBIf: Boolean) extends Module {

  /**
    * AIf設定用のメソッド
    * @param a AIf
    */
  def setA(a: AIf): Unit = {
    when (a.a) {
      a.aa := 0x12345678.U
    } .otherwise {
      a.aa := 0x87654321L.U
    }
  }

  /**
    * BIf設定用のメソッド
    * @param b BIf
    */
  def setB(b: BIf): Unit = {
    when (b.b) {
      b.bb := 0x12345678.U
    } .otherwise {
      b.bb := 0x87654321L.U
    }
  }

  val io = IO(new ABIf(useBIf))

  setA(io.a)
  if (useBIf) { setB(io.b.get) }
}

/**
  * スーパークラスを受け取る設定用のメソッドを用意して、まとめられる設定はまとめてみる
  * @param useBIf BIfを使用するかどうか
  */
class BundleTestModule3(useBIf: Boolean) extends Module {

  /**
    * AIf設定用のメソッド
    * @param a AIf
    */
  def setA(a: AIf): Unit = {
    when (a.a) {
      a.aa := 0x12345678.U
    } .otherwise {
      a.aa := 0x87654321L.U
    }
  }

  /**
    * Bif設定用のメソッド
    * @param b BIf
    */
  def setB(b: BIf): Unit = {
    when (b.b) {
      b.bb := 0x12345678.U
    } .otherwise {
      b.bb := 0x87654321L.U
    }
  }

  /**
    * CIf設定用のメソッド。
    * CIfはBIfを継承しているので、中でsetBを呼び出しBifの設定を行う
    * @param c CIf
    */
  def setC(c: CIf): Unit = {
    setB(c)
    when (c.c) {
      c.cc := 0x12345678.U
    } .otherwise {
      c.cc := 0x87654321L.U
    }
  }

  val io = IO(new ABCIf(useBIf))

  setA(io.a)
  if (useBIf) { setB(io.b.get) }
  setC(io.c)
}
