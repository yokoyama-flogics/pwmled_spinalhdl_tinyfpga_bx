TOPNAME=pwmled_tinyfpga_bx
CLASSNAME=PwmLed_TinyFPGA_BX
# CLASSNAME=PwmLed
PCF=tinyfpga_bx.pcf
ICEVIEW=$(HOME)/Dropbox/monthly/201912/fpga/ice40_viewer/iceview_html.py

$(TOPNAME).asc: $(TOPNAME).json $(PCF)
	nextpnr-ice40 \
		--lp8k \
		--package cm81 \
		--asc $@ \
		--pcf $(PCF) \
		--json $(TOPNAME).json

$(TOPNAME).json: $(CLASSNAME).v
	yosys \
		-ql yosys.log \
		-p 'synth_ice40 -top '$(CLASSNAME)' -json '$@ \
		$^

$(CLASSNAME).v: src/main/scala/PwmLed.scala
	sbt "runMain $(CLASSNAME)"

$(TOPNAME).html: $(TOPNAME).asc
	$(ICEVIEW) $(TOPNAME).asc $@

html: $(TOPNAME).html

$(TOPNAME).bin: $(TOPNAME).asc
	icepack $(TOPNAME).asc $@

upload: $(TOPNAME).bin
	tinyprog -p $^

clean:
	@rm -f $(TOPNAME).asc $(TOPNAME).json $(CLASSNAME).v
