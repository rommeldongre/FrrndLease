ALTER TABLE `requests` DROP PRIMARY KEY, ADD PRIMARY KEY( `request_id`, `request_requser_id`, `request_item_id`);

ALTER TABLE items MODIFY item_image MEDIUMTEXT;

ALTER TABLE items MODIFY item_desc VARCHAR(255) null;

/* 1.user_activation for saving the activation id 2.user_status column for sign up data and status code*/
ALTER TABLE `users` ADD `user_activation` VARCHAR(255) NOT NULL AFTER `user_auth`, ADD `user_status` VARCHAR(255) NOT NULL AFTER `user_activation`;