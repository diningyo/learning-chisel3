module MyQueue
    #(
      parameter p_DATA_BITS  = 'd0,
      parameter p_STRB_BITS  = 'd0,
      parameter p_FIFO_DEPTH = 'd1,
      parameter p_PIPE       = 1'b0,
      parameter p_FLOW       = 1'b0
      )
    (
     input                    clk,
     input                    rst_n,
     // input
     input                    enq_valid,
     output                   enq_ready,
     input [p_STRB_BITS-1:0]  enq_bits_strb,
     input [p_DATA_BITS-1:0]  enq_bits_data,
     input                    enq_bits_last,
     // output
     output                   deq_valid,
     input                    deq_ready,
     output [p_STRB_BITS-1:0] deq_bits_strb,
     output [p_DATA_BITS-1:0] deq_bits_data,
     output                   deq_bits_last,

     output [$clog2(p_FIFO_DEPTH)-1:0] count
     );


    localparam p_QPTR_BITS = $clog2(p_FIFO_DEPTH);

    logic [p_STRB_BITS-1:0]   r_strb_q [0:p_FIFO_DEPTH-1];
    logic [p_DATA_BITS-1:0]   r_data_q [0:p_FIFO_DEPTH-1];
    logic                     r_last_q [0:p_FIFO_DEPTH-1];

    logic                     w_enq;
    logic                     w_deq;
    logic                     w_ctr_match;
    logic                     w_full;
    logic                     w_empty;

    logic [p_QPTR_BITS-1:0]   r_enq_ctr;
    logic [p_QPTR_BITS-1:0]   r_deq_ctr;
    logic                     r_has_data;
    
    always_comb begin
        w_ctr_match = r_enq_ctr == r_deq_ctr;
        w_empty = w_ctr_match && !r_has_data;
        w_full = w_ctr_match && r_has_data;
    end
    
    generate 
        always_ff @(posedge clk, negedge rst_n) begin
            if (!rst_n) begin
                r_enq_ctr <= 'd0;
            end
            else if (w_enq) begin
                if (r_enq_ctr == p_FIFO_DEPTH - 'd1) begin
                    r_enq_ctr <= 'd0;
                end
                else begin
                    r_enq_ctr <= r_enq_ctr + 'd1;
                end
            end
        end // always_ff @ (posedge clk, negedge rst_n)
        
        always_ff @(posedge clk, negedge rst_n) begin
            if (!rst_n) begin
                r_deq_ctr <= 'd0;
            end
            else if (w_deq) begin
                if (r_deq_ctr == p_FIFO_DEPTH - 'd1) begin
                    r_deq_ctr <= 'd0;
                end
                else begin
                    r_deq_ctr <= r_deq_ctr + 'd1;
                end
            end
        end // always_ff @ (posedge clk, negedge rst_n)
    endgenerate
    
    
    always_ff @(posedge clk, negedge rst_n) begin
        if (!rst_n) begin
            r_has_data <= 'd0;
        end
        else if (w_enq != w_deq) begin
            r_has_data <= w_enq;
        end
    end // always_ff @ (posedge clk, negedge rst_n)

    // PIPE
    generate
        if (p_PIPE) begin
            always_comb begin
                enq_ready = (deq_ready) ? 1'b1 : !w_full;
            end
        end
        else begin
            always_comb begin
                enq_ready = !w_full;
            end
        end
    endgenerate
    
    // FLOW
    generate
        if (p_FLOW) begin
            always_comb begin
                deq_valid = (enq_valid) ? 1'b1 : !w_empty;
            end
            
            always_comb begin
                if (w_empty) begin
                    deq_bits_strb = enq_bits_strb;
                    deq_bits_data = enq_bits_data;
                    deq_bits_last = enq_bits_last;

                    w_enq = (deq_ready) ? 1'b0 : enq_valid && enq_ready;
                    w_deq = 1'b0;
                end
                else begin
                    deq_bits_strb = r_strb_q[r_deq_ctr];
                    deq_bits_data = r_data_q[r_deq_ctr];
                    deq_bits_last = r_last_q[r_deq_ctr];

                    w_enq = enq_valid && enq_ready;
                    w_deq = deq_valid && deq_ready;
                end
            end // always_comb
        end
        else begin
            always_comb begin
                deq_valid = !w_empty;
                deq_bits_strb = r_strb_q[r_deq_ctr];
                deq_bits_data = r_data_q[r_deq_ctr];
                deq_bits_last = r_last_q[r_deq_ctr];

                w_enq = enq_valid && enq_ready;
                w_deq = deq_valid && deq_ready;
            end
        end
    endgenerate

    // DEPTH
    generate
        genvar i;
        for (i = 0; i < p_FIFO_DEPTH; i++) begin
            always_ff @(posedge clk, negedge rst_n) begin
                if (!rst_n) begin
                    r_strb_q[i] <= 'h0;
                    r_data_q[i] <= 'h0;
                    r_last_q[i] <= 1'b0;
                end
                else if (w_enq) begin
                    r_strb_q[r_enq_ctr] <= enq_bits_strb;
                    r_data_q[r_enq_ctr] <= enq_bits_data;
                    r_last_q[r_enq_ctr] <= enq_bits_last;
                end
            end // always_ff @ (posedge clk, negedge rst_n)
        end // for (i = 0; i < p_FIFO_DEPTH; i++)
    endgenerate

    assign count = 0;

endmodule // stream_queue
