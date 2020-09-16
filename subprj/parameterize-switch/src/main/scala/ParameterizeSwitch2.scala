import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.util._

// See README.md for license details.

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

protected object State2 extends ChiselEnum {
  val Idle = Value(0.U)
  val PktThrough = Value(1.U)
  val VLAN = Value(2.U)
  val Sectag = Value(3.U)
  val Payload = Value(4.U)
  val Len = Value(5.U)
  val WaitICVCalc = Value(6.U)
}

class PrameteriseSwitch2(p: Boolean) extends Module {

  val io = IO(new Bundle {
    val pkt_through = Input(Bool())
    val hdr_in = Input(Bool())
    val auth_key_valid = Input(Bool())
    val vlan_in = Input(Bool())
    val vlan_exclude = Input(Bool())
    val no_vlan = Input(Bool())
    val sectag_fire = Input(Bool())
    val pl_in = Input(Bool())
    val pb_fire = Input(Bool())
    val pkt_out_fire = Input(Bool())
    val pkt_out_last = if (p) Some(Input(Bool())) else None

    val st = new Bundle {
      val idle = Output(Bool())
      val hdr = if (p) Some(Output(Bool())) else None
      val vlan = Output(Bool())
      val sectag = Output(Bool())
      val pl = Output(Bool())
      val len = Output(Bool())
    }
  })

  val w_done_wait_icv_calc = p match {
    case true => io.pkt_out_fire && io.pkt_out_last.get
    case false => io.pkt_out_fire
  }

  // cast interface
  val r_in_state = RegInit(State2.Idle)

  switch (r_in_state) {
    is (State2.Idle) {

      p match {
        case true =>
          when (io.hdr_in) {
            when(io.pkt_through) {
              r_in_state := State2.PktThrough
            }.elsewhen(io.auth_key_valid) {
              when(io.vlan_exclude) {
                r_in_state := State2.VLAN
              }.otherwise {
                r_in_state := State2.Sectag
              }
            }
          }

        case false =>
          when (io.hdr_in) {
            when (io.auth_key_valid) {
              when (io.vlan_exclude) {
                r_in_state := State2.VLAN
              } .otherwise {
                r_in_state := State2.Sectag
              }
            }
          }
      }
    }

    is (State2.VLAN) {
      p match {
        case true =>
          when(io.vlan_in) {
            r_in_state := State2.Sectag
          }
        case false =>
          when(io.vlan_in) {
            when(io.vlan_exclude) {
              r_in_state := State2.Sectag
            }.otherwise {
              r_in_state := State2.Payload
            }
          }
      }
    }

    is (State2.Sectag) {
      p match {
        case true =>
          when (io.sectag_fire) {
            r_in_state := State2.Payload
          }

        case false =>
          when (!io.vlan_exclude) {
            r_in_state := State2.VLAN
          } .otherwise {
            r_in_state := State2.Payload
          }
      }
    }

    is (State2.Payload) {
      when (io.pl_in) {
        r_in_state := State2.Len
      }
    }

    is (State2.Len) {
      when (io.pb_fire) {
        r_in_state := State2.WaitICVCalc
      }
    }

    is (State2.WaitICVCalc) {
      when (w_done_wait_icv_calc) {
        r_in_state := State2.Idle
      }
    }
  }

  if (p) {
    switch (r_in_state) {
      is (State2.PktThrough) {
        when (io.hdr_in) {
          r_in_state := State2.Idle
        }
      }
    }
  }

  // Output
  io.st.idle := r_in_state === State2.Idle
  if (p) {
    io.st.hdr.get := r_in_state === State2.PktThrough
  }
  io.st.vlan := r_in_state === State2.VLAN
  io.st.sectag := r_in_state === State2.Sectag
  io.st.pl := r_in_state === State2.Payload
  io.st.len := r_in_state === State2.Len
}


object Elaborate2 extends App {
  Driver.execute(Array("-td=rtl", "-tn=true"), () => new PrameteriseSwitch2(true))
  Driver.execute(Array("-td=rtl", "-tn=false"), () => new PrameteriseSwitch2(false))
  //Driver.execute(Array("-td=rtl", "-tn=false"), () => new PrameteriseSwitch(false))
}
