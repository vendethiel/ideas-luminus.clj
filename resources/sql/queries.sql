-- # User Queries
-- :name create-user! :! :n
-- :doc Create a user
insert into users (email, username, pass, admin, is_active)
values (:email, :username, :pass, :is_admin, 1)

-- :name login-user :? :1
-- :doc Get active user by email and password
select * from users
where email = :email and pass = :password and is_active

-- :name get-user-profile :? :1
-- :doc Get the public information of an active user
select id, email, username, admin, last_login
from users
where id = :id and is_active

-- # Category queries
-- :name create-category! :! :n
-- :doc Create a category
insert into categories (name)
values (:name)

-- :name list-categories :? :*
-- :doc List categories
select id, name
from categories

-- :name get-category :? :1
-- :doc Get category by ID
select id, name
from categories
where id = :id

-- :name list-category-ideas :? :*
-- :doc Returns the ideas associated with a category and its comments
select i.id, i.name, i.description, i.tags
from ideas i
inner join idea_category ic
  on ic.idea_id = i.id
 and ic.category_id = :category

-- :name create-idea! :! :n
-- :doc Create an idea
insert into ideas (name, description, tags)
values (:name, :description, :tags)

-- :name link-ideas-categories! :! :n
-- :doc Link ideas to categories
insert into idea_category (idea_id, category_id)
values :tuple*:links

-- :name get-idea-details :? :1
-- :doc Return an idea + comments
select i.id, i.name, i.description, i.tags,
       json_group_array(json_object(
         'user_id', com.user_id,
         'username', cu.username,
         'user_admin', cu.admin,
         'content', com.content,
         'date', com.created_at
       )) comments,
       json_group_array(json_object(
         'id', cat.id,
         'name', cat.name
       )) categories,
       json_group_array(json_object(
         'user_id', imp.user_id,
         'username', impu.username,
         'repo_url', imp.repo_url,
         'demo_url', imp.demo_url,
         'comment', imp.comment,
         'tags', jsonb(imp.tags)
       )) implementations
from ideas i
left join comments com
  on com.parent_type = 'ideas'
 and com.parent_id = i.id
left join users cu
  on cu.id = com.user_id
left join idea_category ic
  on ic.idea_id = i.id
left join categories cat
  on cat.id = ic.category_id
left join implementations imp
  on imp.idea_id = i.id
left join users impu
  on imp.user_id = impu.id
where i.id = :id
group by i.id
