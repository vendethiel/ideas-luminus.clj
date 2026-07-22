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
-- :name create-category! :<!
-- :doc Create a category
insert into categories (name)
values (:name)
returning id

-- :name update-category! :! :1
update categories
set name = :name
where id = :id

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

-- :name create-idea! :<!
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
         'id', imp.id,
         'user_id', imp.user_id,
         'username', impu.username,
         'repo_url', imp.repo_url,
         'demo_url', imp.demo_url,
         'abstract', imp.comment,
         'tags', jsonb(imp.tags)
       )) implementations
from ideas i
left join comments com
  on com.parent_type = 'ideas'
 and com.parent_id = i.id
left join users cu
  on cu.id = com.user_id
 and cu.is_active
left join idea_category ic
  on ic.idea_id = i.id
left join categories cat
  on cat.id = ic.category_id
left join implementations imp
  on imp.idea_id = i.id
left join users impu
  on imp.user_id = impu.id
 and impu.is_active
where i.id = :id
group by i.id

-- :name get-implementation-details :? :1
-- :doc Return an implementation
select imp.repo_url, imp.demo_url, imp.abstract, imp.comment,
       imp.tags,
       impu.id implementation_user,
       impu.username implementation_username,
       idea.id idea_id,
       idea.name idea_name,
       idea.description idea_description,
       idea.tags idea_tags,
       ideau.id idea_user,
       ideau.username idea_username,
       json_group_array(json_object(
         'user_id', com.user_id,
         'username', cu.username,
         'user_admin', cu.admin,
         'content', com.content,
         'date', com.created_at
       )) comments
from implementations imp
left join comments com
  on com.parent_type = 'implementations'
 and com.parent_id = imp.id
left join users cu
  on cu.id = com.user_id
 and cu.is_active
inner join ideas idea
  on idea.id = imp.idea_id
left join users impu
  on impu.id = imp.user_id
 and impu.is_active
left join users ideau
  on ideau.id = idea.user_id
 and ideau.is_active
where imp.id = :id
group by imp.id, idea.id, impu.id, ideau.id
