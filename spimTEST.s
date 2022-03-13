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
addi $sp ,$sp ,-400  
addi $sp ,$sp ,-28  
li $s0 ,0  
sw $s0, 0($sp) 
li $v0 ,5  
syscall 
addi $t0 ,$v0 ,0  
li $t1 ,100  
sw $t0, 4($sp) 
bgt $t0, $t1, return_main 
lw $t0, 4($sp) 
li $t1 ,1  
sub $t0 ,$t0 ,$t1  
li $s0 ,0  
sw $s0, 16($sp) 
sw $t0, 4($sp) 
loop0_main: 
lw $t0, 4($sp) 
lw $t1, 16($sp) 
sw $t0, 4($sp) 
sw $t1, 16($sp) 
bgt $t1, $t0, exit0_main 
lw $t1, 16($sp) 
li $v0 ,5  
syscall 
addi $t0 ,$v0 ,0  
addi $t2 ,$fp ,-436  
sll $t3 ,$t1 ,2  
add $t3 ,$t3 ,$t2  
sw $t0, 0($t3) 
li $t4 ,1  
add $t1 ,$t1 ,$t4  
sw $t1, 16($sp) 
j loop0_main  
exit0_main: 
lw $t0, 4($sp) 
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
addi $a0 ,$fp ,-436  
sw $a0, -60($sp) 
li $a0 ,0  
sw $a0, -64($sp) 
addi $a0 ,$t0 ,0  
sw $a0, -68($sp) 
addi $sp ,$sp ,-68  
jal quicksort  
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
li $s0 ,0  
sw $s0, 16($sp) 
sw $t0, 4($sp) 
loop1_main: 
lw $t0, 4($sp) 
lw $t1, 16($sp) 
sw $t0, 4($sp) 
sw $t1, 16($sp) 
bgt $t1, $t0, exit1_main 
lw $t1, 16($sp) 
addi $t2 ,$fp ,-436  
sll $t3 ,$t1 ,2  
add $t3 ,$t3 ,$t2  
lw $t0, 0($t3) 
li $v0 ,1  
addi $a0 ,$t0 ,0  
syscall 
li $v0 ,11  
li $a0 ,10  
syscall 
li $t4 ,1  
add $t1 ,$t1 ,$t4  
sw $t1, 16($sp) 
j loop1_main  
exit1_main: 
return_main: 
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
quicksort: 
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
addi $sp ,$sp ,-56  
lw $s0, 12($fp) 
sw $s0, 0($sp) 
lw $t0, 8($fp) 
lw $t1, 4($fp) 
li $s0 ,0  
sw $s0, 12($sp) 
li $s0 ,0  
sw $s0, 16($sp) 
sw $t0, 4($sp) 
sw $t1, 8($sp) 
bge $t0, $t1, end_quicksort 
lw $t2, 4($sp) 
lw $t3, 8($sp) 
lw $t4, 0($sp) 
add $t6 ,$t2 ,$t3  
li $t5 ,2  
div $t6 ,$t6 ,$t5  
addi $t0 ,$t4 ,0  
sll $t1 ,$t6 ,2  
add $t1 ,$t1 ,$t0  
lw $s0, 0($t1) 
sw $s0, 36($sp) 
li $t5 ,1  
sub $s0 ,$t2 ,$t5  
sw $s0, 12($sp) 
li $t5 ,1  
add $s0 ,$t3 ,$t5  
sw $s0, 16($sp) 
sw $t2, 4($sp) 
sw $t3, 8($sp) 
sw $t4, 0($sp) 
loop0_quicksort: 
loop1_quicksort: 
lw $t2, 0($sp) 
lw $t5, 12($sp) 
lw $t7, 36($sp) 
li $t6 ,1  
add $t5 ,$t5 ,$t6  
addi $t0 ,$t2 ,0  
sll $t1 ,$t5 ,2  
add $t1 ,$t1 ,$t0  
lw $t3, 0($t1) 
addi $t4 ,$t3 ,0  
sw $t2, 0($sp) 
sw $t4, 44($sp) 
sw $t5, 12($sp) 
sw $t7, 36($sp) 
blt $t4, $t7, loop1_quicksort 
loop2_quicksort: 
lw $t2, 16($sp) 
lw $t3, 0($sp) 
lw $t7, 36($sp) 
li $t6 ,1  
sub $t2 ,$t2 ,$t6  
addi $t0 ,$t3 ,0  
sll $t1 ,$t2 ,2  
add $t1 ,$t1 ,$t0  
lw $t4, 0($t1) 
addi $t5 ,$t4 ,0  
sw $t2, 16($sp) 
sw $t3, 0($sp) 
sw $t5, 48($sp) 
sw $t7, 36($sp) 
bgt $t5, $t7, loop2_quicksort 
lw $t0, 16($sp) 
lw $t1, 12($sp) 
sw $t0, 16($sp) 
sw $t1, 12($sp) 
bge $t1, $t0, exit0_quicksort 
lw $t2, 16($sp) 
lw $t3, 0($sp) 
lw $t4, 44($sp) 
lw $t5, 48($sp) 
lw $t6, 12($sp) 
addi $t0 ,$t3 ,0  
sll $t1 ,$t2 ,2  
add $t1 ,$t1 ,$t0  
sw $t4, 0($t1) 
addi $t0 ,$t3 ,0  
sll $t1 ,$t6 ,2  
add $t1 ,$t1 ,$t0  
sw $t5, 0($t1) 
sw $t2, 16($sp) 
sw $t3, 0($sp) 
sw $t6, 12($sp) 
j loop0_quicksort  
exit0_quicksort: 
lw $t0, 16($sp) 
lw $t1, 4($sp) 
lw $t2, 8($sp) 
lw $t3, 0($sp) 
li $t4 ,1  
add $s0 ,$t0 ,$t4  
sw $s0, 52($sp) 
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
addi $a0 ,$t3 ,0  
sw $a0, -60($sp) 
addi $a0 ,$t1 ,0  
sw $a0, -64($sp) 
addi $a0 ,$t0 ,0  
sw $a0, -68($sp) 
addi $sp ,$sp ,-68  
jal quicksort  
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
li $t4 ,1  
add $t0 ,$t0 ,$t4  
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
addi $a0 ,$t3 ,0  
sw $a0, -60($sp) 
addi $a0 ,$t0 ,0  
sw $a0, -64($sp) 
addi $a0 ,$t2 ,0  
sw $a0, -68($sp) 
addi $sp ,$sp ,-68  
jal quicksort  
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
end_quicksort: 
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
