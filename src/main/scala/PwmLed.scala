// package com.flogics.spinal.pwmled

import spinal.core._
import spinal.lib.fsm._

class Pwm(size: Int) extends Component {
  val io = new Bundle {
    val width = in UInt(size bits)
    val output = out Bool
  }

  val counter = Reg(UInt(size bits)) init (0)

  counter := counter + 1
  io.output := counter < io.width
}


class PwmLed(divSize: Int, pwmSize: Int) extends Component {
  val io = new Bundle {
    val output = out Bool
  }

  val ctDivider = Reg(UInt(divSize bits)) init(0)
  val transFsm = Bool

  ctDivider := ctDivider + 1
  transFsm := ctDivider === U(ctDivider.range -> true)    // eg. 'b11111111

/*
 * Alternative code to implement the above logic for transFsm.
 *
  when (ctDivider === U(ctDivider.range -> true)) {
    transFsm = True
  }.otherwise {
    transFsm = False
  }
*/

  val fsm = new StateMachine {
    val countingUp = new State with EntryPoint
    val countingDown = new State

    val curWidth = Reg(UInt(pwmSize bits)) init(0)    // current PWM width

    countingUp
      .whenIsActive {
        when(transFsm) {
          curWidth := curWidth + 1
          when(curWidth === U(curWidth.range -> true) - 1) {  // eg. 'b11111110
            goto(countingDown)
          }
        }
      }

    countingDown
      .whenIsActive {
        when(transFsm) {
          curWidth := curWidth - 1
          when(curWidth === 1) {    // eg. 'b00000001
            goto(countingUp)
          }
        }
      }
  }

  val pwm = new Pwm(size = pwmSize)
  pwm.io.width <> fsm.curWidth
  pwm.io.output <> io.output
}


class PwmLed_TinyFPGA_BX extends Component {
  val io = new Bundle {
    val CLK = in Bool
    val LED = out Bool
    val USBPU = out Bool
  }

  /*
   * Refer https://wolfgang-jung.net/posts/2018-07-19-spinalhdl/ and
   * https://spinalhdl.github.io/SpinalDoc-RTD/SpinalHDL/Structuring/clock_domain.html
   */
  val coreClockDomain = ClockDomain(
    clock = io.CLK,
    frequency = FixedFrequency(16 MHz),
    config = ClockDomainConfig(
      resetKind = BOOT
    )
  )

  val coreArea = new ClockingArea(coreClockDomain) {
    io.USBPU := False

    val pwmled = new PwmLed(divSize = 12, pwmSize = 8)
    pwmled.io.output <> io.LED
  }
}


object Pwm {
  def main(args: Array[String]): Unit = {
    SpinalVerilog(new Pwm(size = 10))
  }
}


object PwmLed {
  def main(args: Array[String]): Unit = {
    SpinalVerilog(new PwmLed(divSize = 3, pwmSize = 8))
  }
}


object PwmLed_TinyFPGA_BX {
  def main(args: Array[String]): Unit = {
    SpinalVerilog(new PwmLed_TinyFPGA_BX)
  }
}
