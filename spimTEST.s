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
li $vir4 ,0
li $vir1 ,2
li $vir2 ,3
li $vir3 ,6
li $vir12 ,0
li $v0 ,5
syscall
addi $vir5 ,$v0 ,0
li $virBranchTemp ,1
bgt $vir5, $virBranchTemp, label0_main
li $vir6 ,0
addi $vir0 ,$vir6 ,0
j print_main
label0_main:
li $virBranchTemp ,3
bgt $vir5, $virBranchTemp, label1_main
li $vir6 ,1
addi $vir0 ,$vir6 ,0
j print_main
label1_main:
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
addi $sp ,$sp ,-56
addi $a0 ,$vir5 ,0
addi $sp ,$sp ,-4
sw $a0, 0($sp)
addi $a0 ,$vir1 ,0
addi $sp ,$sp ,-4
sw $a0, 0($sp)
jal divisible
addi $vir8 ,$v0 ,0
addi $sp ,$sp ,8
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
addi $vir6 ,$vir12 ,0
addi $vir0 ,$vir6 ,0
li $virBranchTemp ,1
beq $vir8, $virBranchTemp, label2_main
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
addi $sp ,$sp ,-56
addi $a0 ,$vir5 ,0
addi $sp ,$sp ,-4
sw $a0, 0($sp)
addi $a0 ,$vir2 ,0
addi $sp ,$sp ,-4
sw $a0, 0($sp)
jal divisible
addi $vir8 ,$v0 ,0
addi $sp ,$sp ,8
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
addi $vir6 ,$vir12 ,0
addi $vir0 ,$vir6 ,0
li $virBranchTemp ,1
beq $vir8, $virBranchTemp, label2_main
j label3_main
label2_main:
j print_main
label3_main:
li $vir4 ,5
loop_main:
mul $vir7 ,$vir4 ,$vir4
CRASH_main:
bgt $vir7, $vir5, exit_main
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
addi $sp ,$sp ,-56
addi $a0 ,$vir5 ,0
addi $sp ,$sp ,-4
sw $a0, 0($sp)
addi $a0 ,$vir4 ,0
addi $sp ,$sp ,-4
sw $a0, 0($sp)
jal divisible
addi $vir8 ,$v0 ,0
addi $sp ,$sp ,8
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
addi $vir6 ,$vir12 ,0
li $vir10 ,0
li $vir13 ,0
addi $vir0 ,$vir6 ,0
li $virBranchTemp ,1
beq $vir8, $virBranchTemp, label2_main
li $virArithTemp ,2
add $vir9 ,$vir4 ,$virArithTemp
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
addi $sp ,$sp ,-56
addi $a0 ,$vir5 ,0
addi $sp ,$sp ,-4
sw $a0, 0($sp)
addi $a0 ,$vir9 ,0
addi $sp ,$sp ,-4
sw $a0, 0($sp)
jal divisible
addi $vir8 ,$v0 ,0
addi $sp ,$sp ,8
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
addi $vir6 ,$vir12 ,0
addi $vir0 ,$vir6 ,0
li $virBranchTemp ,1
beq $vir8, $virBranchTemp, label2_main
li $virArithTemp ,6
add $vir4 ,$vir4 ,$virArithTemp
j loop_main
exit_main:
addi $vir11 ,$vir10 ,0
addi $vir6 ,$vir13 ,0
li $vir6 ,1
addi $vir0 ,$vir6 ,0
print_main:
li $v0 ,1
addi $a0 ,$vir0 ,0
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
lw $vir3, 8($fp)
lw $vir4, 4($fp)
div $vir2 ,$vir3 ,$vir4
mul $vir2 ,$vir2 ,$vir4
bne $vir3, $vir2, label0_divisible
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