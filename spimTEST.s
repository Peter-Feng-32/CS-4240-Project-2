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
li $t0 ,0  
sw $t0, 0($sp) 
li $v0 ,5  
syscall 
addi $t0 ,$v0 ,0  
sw $t0, 4($sp) 
li $t0 ,100  
sw $t0, 8($sp) 
lw $t1, 4($sp) 
lw $t2, 8($sp) 
bgt $t1, $t2, return_main 
li $t0 ,1  
sw $t0, 12($sp) 
lw $t1, 4($sp) 
lw $t2, 12($sp) 
sub $t0 ,$t1 ,$t2  
sw $t0, 4($sp) 
li $t0 ,0  
sw $t0, 16($sp) 
loop0_main: 
lw $t1, 16($sp) 
lw $t2, 4($sp) 
bgt $t1, $t2, exit0_main 
li $v0 ,5  
syscall 
addi $t0 ,$v0 ,0  
sw $t0, 0($sp) 
addi $t0 ,$fp ,-436  
sw $t0, 20($sp) 
lw $t1, 16($sp) 
sll $t0 ,$t1 ,2  
sw $t0, 24($sp) 
lw $t1, 24($sp) 
lw $t2, 20($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 24($sp) 
lw $t0, 0($sp) 
lw $t2, 24($sp) 
sw $t0, 0($t2) 
li $t0 ,1  
sw $t0, 12($sp) 
lw $t1, 16($sp) 
lw $t2, 12($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 16($sp) 
j loop0_main  
exit0_main: 
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
lw $t1, 4($sp) 
addi $a0 ,$t1 ,0  
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
li $t0 ,0  
sw $t0, 16($sp) 
loop1_main: 
lw $t1, 16($sp) 
lw $t2, 4($sp) 
bgt $t1, $t2, exit1_main 
addi $t0 ,$fp ,-436  
sw $t0, 20($sp) 
lw $t1, 16($sp) 
sll $t0 ,$t1 ,2  
sw $t0, 24($sp) 
lw $t1, 24($sp) 
lw $t2, 20($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 24($sp) 
lw $t2, 24($sp) 
lw $t0, 0($t2) 
sw $t0, 0($sp) 
li $v0 ,1  
lw $t1, 0($sp) 
addi $a0 ,$t1 ,0  
syscall 
li $v0 ,11  
li $a0 ,10  
syscall 
li $t0 ,1  
sw $t0, 12($sp) 
lw $t1, 16($sp) 
lw $t2, 12($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 16($sp) 
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
lw $t0, 12($fp) 
sw $t0, 0($sp) 
lw $t0, 8($fp) 
sw $t0, 4($sp) 
lw $t0, 4($fp) 
sw $t0, 8($sp) 
li $t0 ,0  
sw $t0, 12($sp) 
li $t0 ,0  
sw $t0, 16($sp) 
lw $t1, 4($sp) 
lw $t2, 8($sp) 
bge $t1, $t2, end_quicksort 
lw $t1, 4($sp) 
lw $t2, 8($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 20($sp) 
li $t0 ,2  
sw $t0, 24($sp) 
lw $t1, 20($sp) 
lw $t2, 24($sp) 
div $t0 ,$t1 ,$t2  
sw $t0, 20($sp) 
lw $t1, 0($sp) 
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
li $t0 ,1  
sw $t0, 24($sp) 
lw $t1, 4($sp) 
lw $t2, 24($sp) 
sub $t0 ,$t1 ,$t2  
sw $t0, 12($sp) 
li $t0 ,1  
sw $t0, 24($sp) 
lw $t1, 8($sp) 
lw $t2, 24($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 16($sp) 
loop0_quicksort: 
loop1_quicksort: 
li $t0 ,1  
sw $t0, 24($sp) 
lw $t1, 12($sp) 
lw $t2, 24($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 12($sp) 
lw $t1, 0($sp) 
addi $t0 ,$t1 ,0  
sw $t0, 28($sp) 
lw $t1, 12($sp) 
sll $t0 ,$t1 ,2  
sw $t0, 32($sp) 
lw $t1, 32($sp) 
lw $t2, 28($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 32($sp) 
lw $t2, 32($sp) 
lw $t0, 0($t2) 
sw $t0, 40($sp) 
lw $t1, 40($sp) 
addi $t0 ,$t1 ,0  
sw $t0, 44($sp) 
lw $t1, 44($sp) 
lw $t2, 36($sp) 
blt $t1, $t2, loop1_quicksort 
loop2_quicksort: 
li $t0 ,1  
sw $t0, 24($sp) 
lw $t1, 16($sp) 
lw $t2, 24($sp) 
sub $t0 ,$t1 ,$t2  
sw $t0, 16($sp) 
lw $t1, 0($sp) 
addi $t0 ,$t1 ,0  
sw $t0, 28($sp) 
lw $t1, 16($sp) 
sll $t0 ,$t1 ,2  
sw $t0, 32($sp) 
lw $t1, 32($sp) 
lw $t2, 28($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 32($sp) 
lw $t2, 32($sp) 
lw $t0, 0($t2) 
sw $t0, 40($sp) 
lw $t1, 40($sp) 
addi $t0 ,$t1 ,0  
sw $t0, 48($sp) 
lw $t1, 48($sp) 
lw $t2, 36($sp) 
bgt $t1, $t2, loop2_quicksort 
lw $t1, 12($sp) 
lw $t2, 16($sp) 
bge $t1, $t2, exit0_quicksort 
lw $t1, 0($sp) 
addi $t0 ,$t1 ,0  
sw $t0, 28($sp) 
lw $t1, 16($sp) 
sll $t0 ,$t1 ,2  
sw $t0, 32($sp) 
lw $t1, 32($sp) 
lw $t2, 28($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 32($sp) 
lw $t0, 44($sp) 
lw $t2, 32($sp) 
sw $t0, 0($t2) 
lw $t1, 0($sp) 
addi $t0 ,$t1 ,0  
sw $t0, 28($sp) 
lw $t1, 12($sp) 
sll $t0 ,$t1 ,2  
sw $t0, 32($sp) 
lw $t1, 32($sp) 
lw $t2, 28($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 32($sp) 
lw $t0, 48($sp) 
lw $t2, 32($sp) 
sw $t0, 0($t2) 
j loop0_quicksort  
exit0_quicksort: 
li $t0 ,1  
sw $t0, 24($sp) 
lw $t1, 16($sp) 
lw $t2, 24($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 52($sp) 
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
lw $t1, 0($sp) 
addi $a0 ,$t1 ,0  
sw $a0, -60($sp) 
lw $t1, 4($sp) 
addi $a0 ,$t1 ,0  
sw $a0, -64($sp) 
lw $t1, 16($sp) 
addi $a0 ,$t1 ,0  
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
li $t0 ,1  
sw $t0, 24($sp) 
lw $t1, 16($sp) 
lw $t2, 24($sp) 
add $t0 ,$t1 ,$t2  
sw $t0, 16($sp) 
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
lw $t1, 0($sp) 
addi $a0 ,$t1 ,0  
sw $a0, -60($sp) 
lw $t1, 16($sp) 
addi $a0 ,$t1 ,0  
sw $a0, -64($sp) 
lw $t1, 8($sp) 
addi $a0 ,$t1 ,0  
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
