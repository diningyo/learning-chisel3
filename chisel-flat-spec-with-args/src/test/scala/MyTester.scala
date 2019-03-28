// See LICENSE for license details.

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import org.scalatest.{BeforeAndAfterAllConfigMap, ConfigMap}



class MyTester extends ChiselFlatSpec with BeforeAndAfterAllConfigMap {

  val defaultArgs = scala.collection.mutable.Map[String, Any](
    "--generate-vcd-output" -> "on",
    "--backend-name" -> "verilator"
  )

  override def beforeAll(configMap: ConfigMap): Unit = {
    if (configMap.get("--backend-name").isDefined) {
      defaultArgs("--backend-name") = configMap.get("--backend-name").fold("")(_.toString)
    }
    if (configMap.get("--generate-vcd-output").isDefined) {
      defaultArgs("--generate-vcd-output") = configMap.get("--generate-vcd-output").fold("")(_.toString)
    }
    if (configMap.get("--is-verbose").isDefined) {
      defaultArgs("--is-verbose") = true
    }
  }

  def makeArgs(): Array[String] = {
    defaultArgs.map {
      case (key: String, value: String) => (s"$key=$value")
      case (key: String, value: Boolean) => if (value) key else ""
    }.toArray
  }

  val dataBits = 32
  val fifoStage = 16

  "Module" should "in1とin2を足した値が出てくる、けどそんなのどうでも良い" in {
    Driver.execute(makeArgs(), () => new Module {
      val io = IO(new Bundle {
        val in1 = Input(UInt(16.W))
        val in2 = Input(UInt(16.W))
        val out = Output(UInt(17.W))
      })
      io.out := io.in1 +& io.in2
    }) {
      c => new PeekPokeTester(c) {
        for (i <- 0 until 10) {
          poke(c.io.in1, i)
          poke(c.io.in2, i)
          expect(c.io.out, i + i)
        }
      }
    } should be(true)
  }
}
