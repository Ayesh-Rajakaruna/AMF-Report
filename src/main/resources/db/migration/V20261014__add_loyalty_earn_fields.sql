-- Adds loyaltyEarn values captured after loading loyaltyEarn
ALTER TABLE loyalty_earn_record
  ADD COLUMN plan_contribution      DECIMAL(19,2) NULL,
  ADD COLUMN policy_id              VARCHAR(100)  NULL,
  ADD COLUMN loyalty_program_member VARCHAR(100)  NULL;
