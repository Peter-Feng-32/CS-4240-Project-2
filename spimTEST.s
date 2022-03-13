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
li $vir15 ,0  
li $v0 ,5  
syscall 
addi $vir17 ,$v0 ,0  
li $virBranchTemp ,100  
bgt $vir17, $virBranchTemp, return_main 
li $virArithTemp ,1  
sub $vir17 ,$vir17 ,$virArithTemp  
li $vir16 ,0  
loop0_main: 
bgt $vir16, $vir17, exit0_main 
li $v0 ,5  
syscall 
addi $vir15 ,$v0 ,0  
addi $virArrayBase ,$fp ,-436  
sll $virArrayLocation ,$vir16 ,2  
add $virArrayLocation ,$virArrayLocation ,$virArrayBase  
sw $vir15, 0($virArrayLocation) 
li $virArithTemp ,1  
add $vir16 ,$vir16 ,$virArithTemp  
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
addi $a0 ,$vir17 ,0  
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
li $vir16 ,0  
loop1_main: 
bgt $vir16, $vir17, exit1_main 
addi $virArrayBase ,$fp ,-436  
sll $virArrayLocation ,$vir16 ,2  
add $virArrayLocation ,$virArrayLocation ,$virArrayBase  
lw $vir15, 0($virArrayLocation) 
li $v0 ,1  
addi $a0 ,$vir15 ,0  
syscall 
li $v0 ,11  
li $a0 ,10  
syscall 
li $virArithTemp ,1  
add $vir16 ,$vir16 ,$virArithTemp  
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
addi $sp ,$sp ,-400  
lw $vir3, 12($fp) 
lw $vir5, 8($fp) 
lw $vir4, 4($fp) 
li $vir12 ,0  
li $vir13 ,0  
bge $vir5, $vir4, end_quicksort 
add $vir10 ,$vir5 ,$vir4  
li $virArithTemp ,2  
div $vir10 ,$vir10 ,$virArithTemp  
addi $virArrayBase ,$fp ,-436  
sll $virArrayLocation ,$vir10 ,2  
add $virArrayLocation ,$virArrayLocation ,$virArrayBase  
lw $vir11, 0($virArrayLocation) 
li $virArithTemp ,1  
sub $vir12 ,$vir5 ,$virArithTemp  
li $virArithTemp ,1  
add $vir13 ,$vir4 ,$virArithTemp  
loop0_quicksort: 
loop1_quicksort: 
li $virArithTemp ,1  
add $vir12 ,$vir12 ,$virArithTemp  
addi $virArrayBase ,$fp ,-436  
sll $virArrayLocation ,$vir12 ,2  
add $virArrayLocation ,$virArrayLocation ,$virArrayBase  
lw $vir9, 0($virArrayLocation) 
addi $vir6 ,$vir9 ,0  
blt $vir6, $vir11, loop1_quicksort 
loop2_quicksort: 
li $virArithTemp ,1  
sub $vir13 ,$vir13 ,$virArithTemp  
addi $virArrayBase ,$fp ,-436  
sll $virArrayLocation ,$vir13 ,2  
add $virArrayLocation ,$virArrayLocation ,$virArrayBase  
lw $vir9, 0($virArrayLocation) 
addi $vir7 ,$vir9 ,0  
bgt $vir7, $vir11, loop2_quicksort 
bge $vir12, $vir13, exit0_quicksort 
addi $virArrayBase ,$fp ,-436  
sll $virArrayLocation ,$vir13 ,2  
add $virArrayLocation ,$virArrayLocation ,$virArrayBase  
sw $vir6, 0($virArrayLocation) 
addi $virArrayBase ,$fp ,-436  
sll $virArrayLocation ,$vir12 ,2  
add $virArrayLocation ,$virArrayLocation ,$virArrayBase  
sw $vir7, 0($virArrayLocation) 
j loop0_quicksort  
exit0_quicksort: 
li $virArithTemp ,1  
add $vir8 ,$vir13 ,$virArithTemp  
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
addi $a0 ,$vir5 ,0  
sw $a0, -64($sp) 
addi $a0 ,$vir13 ,0  
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
li $virArithTemp ,1  
add $vir13 ,$vir13 ,$virArithTemp  
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
addi $a0 ,$vir13 ,0  
sw $a0, -64($sp) 
addi $a0 ,$vir4 ,0  
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
