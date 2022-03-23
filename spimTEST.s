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
addi $sp ,$sp ,-20  
addi $sp ,$sp ,-20  
addi $sp ,$sp ,-20  
li $vir9 ,5  
start_x_init_main: 
li $virBranchTemp ,0  
blt $vir9, $virBranchTemp, end_x_init_main 
li $virArithTemp ,5  
sub $vir7 ,$virArithTemp ,$vir9  
addi $virArrayBase ,$fp ,-56  
addi $virArrayAssignTempCounter ,$vir9 ,0  
li $virArrayAssignRunningOffset ,0  
ARRAY_ASSIGN_START_0: 
li $virBranchTemp ,0  
beq $virArrayAssignTempCounter, $virBranchTemp, ARRAY_ASSIGN_END_0 
add $virArrayBasePlusOffset ,$virArrayBase ,$virArrayAssignRunningOffset  
sw $vir7, 0($virArrayBasePlusOffset) 
addi $virArrayAssignTempCounter ,$virArrayAssignTempCounter ,-1  
addi $virArrayAssignRunningOffset ,$virArrayAssignRunningOffset ,4  
j ARRAY_ASSIGN_START_0  
ARRAY_ASSIGN_END_0: 
li $virArithTemp ,1  
sub $vir9 ,$vir9 ,$virArithTemp  
j start_x_init_main  
end_x_init_main: 
li $vir9 ,5  
start_y_init_main: 
li $virBranchTemp ,0  
blt $vir9, $virBranchTemp, end_y_init_main 
addi $virArrayBase ,$fp ,-76  
addi $virArrayAssignTempCounter ,$vir9 ,0  
li $virArrayAssignRunningOffset ,0  
ARRAY_ASSIGN_START_1: 
li $virBranchTemp ,0  
beq $virArrayAssignTempCounter, $virBranchTemp, ARRAY_ASSIGN_END_1 
add $virArrayBasePlusOffset ,$virArrayBase ,$virArrayAssignRunningOffset  
sw $vir9, 0($virArrayBasePlusOffset) 
addi $virArrayAssignTempCounter ,$virArrayAssignTempCounter ,-1  
addi $virArrayAssignRunningOffset ,$virArrayAssignRunningOffset ,4  
j ARRAY_ASSIGN_START_1  
ARRAY_ASSIGN_END_1: 
li $virArithTemp ,1  
sub $vir9 ,$vir9 ,$virArithTemp  
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
addi $a0 ,$fp ,-56  
sw $a0, -60($sp) 
addi $a0 ,$fp ,-76  
sw $a0, -64($sp) 
addi $a0 ,$fp ,-96  
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
li $vir9 ,0  
start_print_main: 
li $virBranchTemp ,5  
bge $vir9, $virBranchTemp, end_print_main 
addi $virArrayBase ,$fp ,-96  
sll $virArrayLocation ,$vir9 ,2  
add $virArrayLocation ,$virArrayLocation ,$virArrayBase  
lw $vir7, 0($virArrayLocation) 
li $v0 ,1  
addi $a0 ,$vir7 ,0  
syscall 
li $vir7 ,10  
li $v0 ,11  
addi $a0 ,$vir7 ,0  
syscall 
li $virArithTemp ,1  
add $vir9 ,$vir9 ,$virArithTemp  
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
lw $vir0, 12($fp) 
lw $vir1, 8($fp) 
lw $vir2, 4($fp) 
li $vir6 ,0  
start_loop_f: 
li $virBranchTemp ,5  
bge $vir6, $virBranchTemp, end_loop_f 
addi $virArrayBase ,$vir0 ,0  
sll $virArrayLocation ,$vir6 ,2  
add $virArrayLocation ,$virArrayLocation ,$virArrayBase  
lw $vir3, 0($virArrayLocation) 
addi $virArrayBase ,$vir1 ,0  
sll $virArrayLocation ,$vir6 ,2  
add $virArrayLocation ,$virArrayLocation ,$virArrayBase  
lw $vir4, 0($virArrayLocation) 
add $vir5 ,$vir3 ,$vir4  
addi $virArrayBase ,$vir2 ,0  
sll $virArrayLocation ,$vir6 ,2  
add $virArrayLocation ,$virArrayLocation ,$virArrayBase  
sw $vir5, 0($virArrayLocation) 
li $virArithTemp ,1  
add $vir6 ,$vir6 ,$virArithTemp  
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
