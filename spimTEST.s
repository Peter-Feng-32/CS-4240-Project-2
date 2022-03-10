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
addi $sp ,$sp ,-64  
lw $t0, 0($sp) 
li $t0 ,0  
sw $t0, 0($sp) 
lw $t0, 4($sp) 
li $t0 ,2  
sw $t0, 4($sp) 
lw $t0, 8($sp) 
li $t0 ,3  
sw $t0, 8($sp) 
lw $t0, 12($sp) 
li $t0 ,6  
sw $t0, 12($sp) 
lw $t0, 16($sp) 
li $t0 ,0  
sw $t0, 16($sp) 
li $v0 ,5  
syscall 
lw $t0, 20($sp) 
addi $t0 ,$v0 ,0  
sw $t0, 20($sp) 
lw $t0, 24($sp) 
li $t0 ,1  
sw $t0, 24($sp) 
lw $t1, 20($sp) 
lw $t2, 24($sp) 
bgt $t1, $t2, label0_main 
sw $t2, 24($sp) 
sw $t1, 20($sp) 
lw $t0, 28($sp) 
li $t0 ,0  
sw $t0, 28($sp) 
lw $t0, 32($sp) 
lw $t1, 28($sp) 
addi $t0 ,$t1 ,0  
sw $t1, 28($sp) 
sw $t0, 32($sp) 
j print_main  
label0_main: 
lw $t0, 24($sp) 
li $t0 ,3  
sw $t0, 24($sp) 
lw $t1, 20($sp) 
lw $t2, 24($sp) 
bgt $t1, $t2, label1_main 
sw $t2, 24($sp) 
sw $t1, 20($sp) 
lw $t0, 28($sp) 
li $t0 ,1  
sw $t0, 28($sp) 
lw $t0, 32($sp) 
lw $t1, 28($sp) 
addi $t0 ,$t1 ,0  
sw $t1, 28($sp) 
sw $t0, 32($sp) 
j print_main  
label1_main: 
lw $t1, 20($sp) 
addi $a0 ,$t1 ,0  
sw $t1, 20($sp) 
sw $a0, -4($sp) 
lw $t1, 4($sp) 
addi $a0 ,$t1 ,0  
sw $t1, 4($sp) 
sw $a0, -8($sp) 
addi $sp ,$sp ,-8  
jal divisible  
addi $sp ,$sp ,8  
lw $t0, 36($sp) 
addi $t0 ,$v0 ,0  
sw $t0, 36($sp) 
lw $t0, 28($sp) 
lw $t1, 16($sp) 
addi $t0 ,$t1 ,0  
sw $t1, 16($sp) 
sw $t0, 28($sp) 
lw $t0, 32($sp) 
lw $t1, 28($sp) 
addi $t0 ,$t1 ,0  
sw $t1, 28($sp) 
sw $t0, 32($sp) 
lw $t0, 24($sp) 
li $t0 ,1  
sw $t0, 24($sp) 
lw $t1, 36($sp) 
lw $t2, 24($sp) 
beq $t1, $t2, label2_main 
sw $t2, 24($sp) 
sw $t1, 36($sp) 
lw $t1, 20($sp) 
addi $a0 ,$t1 ,0  
sw $t1, 20($sp) 
sw $a0, -4($sp) 
lw $t1, 8($sp) 
addi $a0 ,$t1 ,0  
sw $t1, 8($sp) 
sw $a0, -8($sp) 
addi $sp ,$sp ,-8  
jal divisible  
addi $sp ,$sp ,8  
lw $t0, 36($sp) 
addi $t0 ,$v0 ,0  
sw $t0, 36($sp) 
lw $t0, 28($sp) 
lw $t1, 16($sp) 
addi $t0 ,$t1 ,0  
sw $t1, 16($sp) 
sw $t0, 28($sp) 
lw $t0, 32($sp) 
lw $t1, 28($sp) 
addi $t0 ,$t1 ,0  
sw $t1, 28($sp) 
sw $t0, 32($sp) 
lw $t0, 24($sp) 
li $t0 ,1  
sw $t0, 24($sp) 
lw $t1, 36($sp) 
lw $t2, 24($sp) 
beq $t1, $t2, label2_main 
sw $t2, 24($sp) 
sw $t1, 36($sp) 
j label3_main  
label2_main: 
j print_main  
label3_main: 
lw $t0, 0($sp) 
li $t0 ,5  
sw $t0, 0($sp) 
loop_main: 
lw $t0, 40($sp) 
lw $t1, 0($sp) 
lw $t2, 0($sp) 
mul $t0 ,$t1 ,$t2  
sw $t2, 0($sp) 
sw $t1, 0($sp) 
sw $t0, 40($sp) 
CRASH_main: 
lw $t1, 40($sp) 
lw $t2, 20($sp) 
bgt $t1, $t2, exit_main 
sw $t2, 20($sp) 
sw $t1, 40($sp) 
lw $t1, 20($sp) 
addi $a0 ,$t1 ,0  
sw $t1, 20($sp) 
sw $a0, -4($sp) 
lw $t1, 0($sp) 
addi $a0 ,$t1 ,0  
sw $t1, 0($sp) 
sw $a0, -8($sp) 
addi $sp ,$sp ,-8  
jal divisible  
addi $sp ,$sp ,8  
lw $t0, 36($sp) 
addi $t0 ,$v0 ,0  
sw $t0, 36($sp) 
lw $t0, 28($sp) 
lw $t1, 16($sp) 
addi $t0 ,$t1 ,0  
sw $t1, 16($sp) 
sw $t0, 28($sp) 
lw $t0, 44($sp) 
li $t0 ,0  
sw $t0, 44($sp) 
lw $t0, 48($sp) 
li $t0 ,0  
sw $t0, 48($sp) 
lw $t0, 32($sp) 
lw $t1, 28($sp) 
addi $t0 ,$t1 ,0  
sw $t1, 28($sp) 
sw $t0, 32($sp) 
lw $t0, 24($sp) 
li $t0 ,1  
sw $t0, 24($sp) 
lw $t1, 36($sp) 
lw $t2, 24($sp) 
beq $t1, $t2, label2_main 
sw $t2, 24($sp) 
sw $t1, 36($sp) 
lw $t0, 52($sp) 
li $t0 ,2  
sw $t0, 52($sp) 
lw $t0, 56($sp) 
lw $t1, 0($sp) 
lw $t2, 52($sp) 
add $t0 ,$t1 ,$t2  
sw $t2, 52($sp) 
sw $t1, 0($sp) 
sw $t0, 56($sp) 
lw $t1, 20($sp) 
addi $a0 ,$t1 ,0  
sw $t1, 20($sp) 
sw $a0, -4($sp) 
lw $t1, 56($sp) 
addi $a0 ,$t1 ,0  
sw $t1, 56($sp) 
sw $a0, -8($sp) 
addi $sp ,$sp ,-8  
jal divisible  
addi $sp ,$sp ,8  
lw $t0, 36($sp) 
addi $t0 ,$v0 ,0  
sw $t0, 36($sp) 
lw $t0, 28($sp) 
lw $t1, 16($sp) 
addi $t0 ,$t1 ,0  
sw $t1, 16($sp) 
sw $t0, 28($sp) 
lw $t0, 32($sp) 
lw $t1, 28($sp) 
addi $t0 ,$t1 ,0  
sw $t1, 28($sp) 
sw $t0, 32($sp) 
lw $t0, 24($sp) 
li $t0 ,1  
sw $t0, 24($sp) 
lw $t1, 36($sp) 
lw $t2, 24($sp) 
beq $t1, $t2, label2_main 
sw $t2, 24($sp) 
sw $t1, 36($sp) 
lw $t0, 52($sp) 
li $t0 ,6  
sw $t0, 52($sp) 
lw $t0, 0($sp) 
lw $t1, 0($sp) 
lw $t2, 52($sp) 
add $t0 ,$t1 ,$t2  
sw $t2, 52($sp) 
sw $t1, 0($sp) 
sw $t0, 0($sp) 
j loop_main  
exit_main: 
lw $t0, 60($sp) 
lw $t1, 44($sp) 
addi $t0 ,$t1 ,0  
sw $t1, 44($sp) 
sw $t0, 60($sp) 
lw $t0, 28($sp) 
lw $t1, 48($sp) 
addi $t0 ,$t1 ,0  
sw $t1, 48($sp) 
sw $t0, 28($sp) 
lw $t0, 28($sp) 
li $t0 ,1  
sw $t0, 28($sp) 
lw $t0, 32($sp) 
lw $t1, 28($sp) 
addi $t0 ,$t1 ,0  
sw $t1, 28($sp) 
sw $t0, 32($sp) 
print_main: 
li $v0 ,1  
lw $t1, 32($sp) 
addi $a0 ,$t1 ,0  
sw $t1, 32($sp) 
syscall 
li $v0 ,11  
li $a0 ,10  
syscall 
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
divisible: 
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
addi $sp ,$sp ,-12  
lw $t0, 0($sp) 
lw $t0, 8($fp) 
sw $t0, 0($sp) 
lw $t0, 4($sp) 
lw $t0, 4($fp) 
sw $t0, 4($sp) 
lw $t0, 8($sp) 
lw $t1, 0($sp) 
lw $t2, 4($sp) 
div $t0 ,$t1 ,$t2  
sw $t2, 4($sp) 
sw $t1, 0($sp) 
sw $t0, 8($sp) 
lw $t0, 8($sp) 
lw $t1, 8($sp) 
lw $t2, 4($sp) 
mul $t0 ,$t1 ,$t2  
sw $t2, 4($sp) 
sw $t1, 8($sp) 
sw $t0, 8($sp) 
lw $t1, 0($sp) 
lw $t2, 8($sp) 
bne $t1, $t2, label0_divisible 
sw $t2, 8($sp) 
sw $t1, 0($sp) 
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
li $v0 ,1  
jr $ra  
label0_divisible: 
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
li $v0 ,0  
jr $ra  
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
