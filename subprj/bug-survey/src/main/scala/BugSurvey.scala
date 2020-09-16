// See LICENSE for license details.

import chisel3._
import chisel3.util._

class BugSurveyIO(enIn2: Boolean) extends Bundle {
  val in1 = Input(Bool())
  val in2 = if (enIn2) Some(Input(Bool())) else None
  val in3 = Input(UInt(8.W))
  val out1 = Output(Bool())
  val out2 = if (enIn2) Some(Output(Bool())) else None

  override def cloneType: this.type =
    new BugSurveyIO(enIn2).asInstanceOf[this.type]
}

class BugSurvey(val enIn2: Boolean) extends Module {
  val io = IO(new BugSurveyIO(enIn2))

  io.out1 := io.in1
  if (enIn2) {
    io.out2.get := io.in2.get
  }
}
