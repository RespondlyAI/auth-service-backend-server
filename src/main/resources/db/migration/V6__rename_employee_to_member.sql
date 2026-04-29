-- V6: Rename EMPLOYEE role to MEMBER
UPDATE roles 
SET name = 'MEMBER' 
WHERE name = 'EMPLOYEE';
