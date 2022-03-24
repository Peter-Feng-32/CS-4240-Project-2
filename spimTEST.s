.text
main: 
sw $fp, -4($sp) 
addi $sp ,$sp ,-4  
addi $fp ,$sp ,0  
sw $ra, -4($sp) 
sw $s0, -8($sp) 
sw $s1, -12($sp) 
sw $s2, -16($sp) 
sw $s3, -20($sp) 
sw $s4, -24($sp) 
sw $s5, -28($sp) 
sw $s6, -32($sp) 
sw $s7, -36($sp) 
addi $sp ,$sp ,-36  
addi $sp ,$sp ,-44  
li $t0 ,-20  
sw $t0, 0($sp) 
lw $t2, 0($sp) 
add $sp ,$sp ,$t2  
li $t0 ,-20  
sw $t0, 0($sp) 
lw $t2, 0($sp) 
add $sp ,$sp ,$t2  
li $t0 ,-20  
sw $t0, 0($sp) 
lw $t2, 0($sp) 
add $sp ,$sp ,$t2  
li $t0 ,5  
sw $t0, 4($sp) 
start_x_init_main: 
li $t0 ,0  
sw $t0, 8($sp) 
lw $t1, 4($sp) 
lw $t2, 8($sp) 
blt $t1, $t2, end_x_init_main 
li $t0 ,5  
sw $t0, 12($sp) 
lw $t1, 12($sp) 
lw $t2, 4($sp) 
sub $t0 ,$t1 ,$t2  
sw $t0, 16($sp) 
li $t0 ,-56  
sw $t0, 0($sp) 
lw $t2, 0($sp) 
add $t0 ,$fp ,$t2  
sw $t0, 20($sp) 
lw $t1, 4($sp) 
addi $t0 ,$t1 ,0  
sw $t0, 24($sp) 
li $t0 ,0  
sw $t0, 28($sp) 
ARRAY_ASSIGN_START_0: 
li $t0 ,0  
sw $t0, 8($sp) 
lw $t1, 24($sp) 
lw $t2, 8($sp) 
beq $t1, $t2, ARRAY_ASSIGN_END_0 
lw $t1, 20($sp) 
lw $t2, 28($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 32($sp) 
lw $t0, 16($sp) 
lw $t2, 32($sp) 
sw $t0, 0($t2) 
lw $t1, 24($sp) 
addi $t0 ,$t1 ,-1  
sw $t0, 24($sp) 
lw $t1, 28($sp) 
addi $t0 ,$t1 ,4  
sw $t0, 28($sp) 
j ARRAY_ASSIGN_START_0  
ARRAY_ASSIGN_END_0: 
li $t0 ,1  
sw $t0, 12($sp) 
lw $t1, 4($sp) 
lw $t2, 12($sp) 
sub $t0 ,$t1 ,$t2  
sw $t0, 4($sp) 
j start_x_init_main  
end_x_init_main: 
li $t0 ,5  
sw $t0, 4($sp) 
start_y_init_main: 
li $t0 ,0  
sw $t0, 8($sp) 
lw $t1, 4($sp) 
lw $t2, 8($sp) 
blt $t1, $t2, end_y_init_main 
li $t0 ,-76  
sw $t0, 0($sp) 
lw $t2, 0($sp) 
add $t0 ,$fp ,$t2  
sw $t0, 20($sp) 
lw $t1, 4($sp) 
addi $t0 ,$t1 ,0  
sw $t0, 24($sp) 
li $t0 ,0  
sw $t0, 28($sp) 
ARRAY_ASSIGN_START_1: 
li $t0 ,0  
sw $t0, 8($sp) 
lw $t1, 24($sp) 
lw $t2, 8($sp) 
beq $t1, $t2, ARRAY_ASSIGN_END_1 
lw $t1, 20($sp) 
lw $t2, 28($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 32($sp) 
lw $t0, 4($sp) 
lw $t2, 32($sp) 
sw $t0, 0($t2) 
lw $t1, 24($sp) 
addi $t0 ,$t1 ,-1  
sw $t0, 24($sp) 
lw $t1, 28($sp) 
addi $t0 ,$t1 ,4  
sw $t0, 28($sp) 
j ARRAY_ASSIGN_START_1  
ARRAY_ASSIGN_END_1: 
li $t0 ,1  
sw $t0, 12($sp) 
lw $t1, 4($sp) 
lw $t2, 12($sp) 
sub $t0 ,$t1 ,$t2  
sw $t0, 4($sp) 
j start_y_init_main  
end_y_init_main: 
sw $a0, -4($sp) 
sw $a1, -8($sp) 
sw $a2, -12($sp) 
sw $a3, -16($sp) 
sw $t0, -20($sp) 
sw $t1, -24($sp) 
sw $t2, -28($sp) 
sw $t3, -32($sp) 
sw $t4, -36($sp) 
sw $t5, -40($sp) 
sw $t6, -44($sp) 
sw $t7, -48($sp) 
sw $t8, -52($sp) 
sw $t9, -56($sp) 
li $t0 ,-56  
sw $t0, 0($sp) 
lw $t2, 0($sp) 
add $t0 ,$fp ,$t2  
sw $t0, 36($sp) 
sw $a0, -60($sp) 
li $t0 ,-76  
sw $t0, 0($sp) 
lw $t2, 0($sp) 
add $t0 ,$fp ,$t2  
sw $t0, 36($sp) 
sw $a0, -64($sp) 
li $t0 ,-96  
sw $t0, 0($sp) 
lw $t2, 0($sp) 
add $t0 ,$fp ,$t2  
sw $t0, 36($sp) 
sw $a0, -68($sp) 
addi $sp ,$sp ,-68  
jal f  
addi $sp ,$sp ,12  
addi $sp ,$sp ,56  
lw $a0, -4($sp) 
lw $a1, -8($sp) 
lw $a2, -12($sp) 
lw $a3, -16($sp) 
lw $t0, -20($sp) 
lw $t1, -24($sp) 
lw $t2, -28($sp) 
lw $t3, -32($sp) 
lw $t4, -36($sp) 
lw $t5, -40($sp) 
lw $t6, -44($sp) 
lw $t7, -48($sp) 
lw $t8, -52($sp) 
lw $t9, -56($sp) 
li $t0 ,0  
sw $t0, 4($sp) 
start_print_main: 
li $t0 ,5  
sw $t0, 8($sp) 
lw $t1, 4($sp) 
lw $t2, 8($sp) 
bge $t1, $t2, end_print_main 
li $t0 ,-96  
sw $t0, 0($sp) 
lw $t2, 0($sp) 
add $t0 ,$fp ,$t2  
sw $t0, 20($sp) 
lw $t1, 4($sp) 
sll $t0 ,$t1 ,2  
sw $t0, 40($sp) 
lw $t1, 40($sp) 
lw $t2, 20($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 40($sp) 
lw $t2, 40($sp) 
lw $t0, 0($t2) 
sw $t0, 16($sp) 
li $v0 ,1  
lw $t1, 16($sp) 
addi $a0 ,$t1 ,0  
syscall 
li $t0 ,10  
sw $t0, 16($sp) 
li $v0 ,11  
lw $t1, 16($sp) 
addi $a0 ,$t1 ,0  
syscall 
li $t0 ,1  
sw $t0, 12($sp) 
lw $t1, 4($sp) 
lw $t2, 12($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 4($sp) 
j start_print_main  
end_print_main: 
lw $s0, -8($fp) 
lw $s1, -12($fp) 
lw $s2, -16($fp) 
lw $s3, -20($fp) 
lw $s4, -24($fp) 
lw $s5, -28($fp) 
lw $s6, -32($fp) 
lw $s7, -36($fp) 
lw $ra, -4($fp) 
addi $sp ,$fp ,4  
lw $fp, 0($fp) 
li $v0 ,10  
syscall 
f: 
sw $fp, -4($sp) 
addi $sp ,$sp ,-4  
addi $fp ,$sp ,0  
sw $ra, -4($sp) 
sw $s0, -8($sp) 
sw $s1, -12($sp) 
sw $s2, -16($sp) 
sw $s3, -20($sp) 
sw $s4, -24($sp) 
sw $s5, -28($sp) 
sw $s6, -32($sp) 
sw $s7, -36($sp) 
addi $sp ,$sp ,-36  
addi $sp ,$sp ,-52  
li $t0 ,12  
sw $t0, 0($sp) 
lw $t1, 0($sp) 
add $t0 ,$t1 ,$fp  
sw $t0, 4($sp) 
lw $t2, 4($sp) 
lw $t0, 0($t2) 
sw $t0, 8($sp) 
li $t0 ,8  
sw $t0, 0($sp) 
lw $t1, 0($sp) 
add $t0 ,$t1 ,$fp  
sw $t0, 4($sp) 
lw $t2, 4($sp) 
lw $t0, 0($t2) 
sw $t0, 12($sp) 
li $t0 ,4  
sw $t0, 0($sp) 
lw $t1, 0($sp) 
add $t0 ,$t1 ,$fp  
sw $t0, 4($sp) 
lw $t2, 4($sp) 
lw $t0, 0($t2) 
sw $t0, 16($sp) 
li $t0 ,0  
sw $t0, 20($sp) 
start_loop_f: 
li $t0 ,5  
sw $t0, 24($sp) 
lw $t1, 20($sp) 
lw $t2, 24($sp) 
bge $t1, $t2, end_loop_f 
lw $t1, 8($sp) 
addi $t0 ,$t1 ,0  
sw $t0, 28($sp) 
lw $t1, 20($sp) 
sll $t0 ,$t1 ,2  
sw $t0, 32($sp) 
lw $t1, 32($sp) 
lw $t2, 28($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 32($sp) 
lw $t2, 32($sp) 
lw $t0, 0($t2) 
sw $t0, 36($sp) 
lw $t1, 12($sp) 
addi $t0 ,$t1 ,0  
sw $t0, 28($sp) 
lw $t1, 20($sp) 
sll $t0 ,$t1 ,2  
sw $t0, 32($sp) 
lw $t1, 32($sp) 
lw $t2, 28($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 32($sp) 
lw $t2, 32($sp) 
lw $t0, 0($t2) 
sw $t0, 40($sp) 
lw $t1, 36($sp) 
lw $t2, 40($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 44($sp) 
lw $t1, 16($sp) 
addi $t0 ,$t1 ,0  
sw $t0, 28($sp) 
lw $t1, 20($sp) 
sll $t0 ,$t1 ,2  
sw $t0, 32($sp) 
lw $t1, 32($sp) 
lw $t2, 28($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 32($sp) 
lw $t0, 44($sp) 
lw $t2, 32($sp) 
sw $t0, 0($t2) 
li $t0 ,1  
sw $t0, 48($sp) 
lw $t1, 20($sp) 
lw $t2, 48($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 20($sp) 
j start_loop_f  
end_loop_f: 
lw $s0, -8($fp) 
lw $s1, -12($fp) 
lw $s2, -16($fp) 
lw $s3, -20($fp) 
lw $s4, -24($fp) 
lw $s5, -28($fp) 
lw $s6, -32($fp) 
lw $s7, -36($fp) 
lw $ra, -4($fp) 
addi $sp ,$fp ,4  
lw $fp, 0($fp) 
jr $ra  
