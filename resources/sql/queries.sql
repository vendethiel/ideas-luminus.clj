-- # User Queries
-- :name create-user! :! :n
-- :doc Create a user
insert into users (email, username, pass, admin, is_active)
values (:email, :username, :pass, :is_admin, 1)
returning id

-- :name login-user :? :1
-- :doc Get active user by email and password
select id from users
where email = :email
  and pass = :pass
  and is_active

-- :name get-user-profile :? :1
-- :doc Get the public information of an active user TODO RENAME
select id, email, username, admin, last_login
from users
where id = :id
--~ (when (not (:admin-query? params)) "and is_active")

-- :name get-user-detailed :? :1
-- :doc Get a user's complete profile
select u.id, u.username,
       json_group_array(json_object(
         'repo_url', imp.repo_url,
         'demo_url', imp.demo_url,
         'abstract', imp.abstract,
         'tags', jsonb(imp.tags),
         'idea_id', impi.id,
         'idea_name', impi.name,
         'idea_description', impi.description,
         'idea_tags', jsonb(impi.tags)
       )) filter (
          where imp.id is not null
            and impi.id is not null
       ) implementations

from users u
left join implementations imp
  on imp.user_id = u.id
left join ideas impi
  on imp.idea_id = impi.id
where u.id = :id
group by u.id
--~ (when (not (:admin-query? params)) "and is_active")

-- :name get-user-comments :? :*
-- :doc Returns the last comments of a user, and the associated object
select com.created_at, com.content,
       coalesce(impi.id, i.id) idea_id,
       coalesce(impi.name, i.name) idea_name,
       imp.id imp_id,
       imp.abstract imp_abstract

from comments com
left join ideas i
  on com.parent_type = 'ideas' and com.parent_id = i.id
left join implementations imp
  on com.parent_type = 'implementations' and com.parent_id = imp.id
left join ideas impi
  on impi.id = imp.idea_id
where com.user_id = :id
order by created_at desc
limit :limit

-- # Category queries
-- :name create-category! :<!
-- :doc Create a category
insert into categories (name)
values (:name)
returning id

-- :name update-category! :! :1
-- :doc Updates a category
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
insert into ideas (name, description, tags, user_id)
values (:name, :description, :tags, :user_id)
returning id

-- :name update-idea! :! :1
-- :doc Update an idea TODO tags too
update ideas
set name = :name
where id = :id

-- :name list-unassigned-ideas :? :*
-- :doc List ideas that don't have a category
select ideas.id, ideas.name, ideas.description, ideas.tags, ideas.user_id,
       u.username user_name
from ideas
left join users u
  on u.id = ideas.user_id
where not exists (
  select 1
  from idea_category ic
  where ic.idea_id = ideas.id
)
limit :limit
-- TODO created at

-- :name list-idea-categories :? :*
-- :doc Lists which categories are assigned to an idea
select category_id
from idea_category
where idea_id = :idea

-- :name link-ideas-categories! :! :n
-- :doc Link ideas to categories
insert into idea_category (idea_id, category_id)
values :tuple*:links

-- :name unlink-idea-categories! :! :n
-- :doc Unlink an idea from specific categories
delete from idea_category
where idea_id = :idea
  and category_id in (:v*:categories)

-- :name get-idea :? :1
-- :doc Returns a simple idea
select *
from ideas
where id = :id

-- :name get-idea-details :? :1
-- :doc Return an idea and its linked components
select i.id, i.name, i.description, i.tags,
       json_group_array(json_object(
         'user_id', com.user_id,
         'username', cu.username,
         'user_admin', cu.admin,
         'content', com.content,
         'date', com.created_at
       )) filter (
         where com.id is not null and cu.id is not null
       ) comments,
       json_group_array(json_object(
         'id', cat.id,
         'name', cat.name
       )) filter (
         where cat.id is not null
       ) categories,
       json_group_array(json_object(
         'id', imp.id,
         'user_id', imp.user_id,
         'username', impu.username,
         'repo_url', imp.repo_url,
         'demo_url', imp.demo_url,
         'abstract', imp.abstract,
         'tags', jsonb(imp.tags)
       )) filter (
         where imp.id is not null
       ) implementations

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

-- :name get-implementation :? :1
-- :doc Return an implementation
select *
from implementations
where id = :id

-- :name get-implementation-details :? :1
-- :doc Return an implementation and its details
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
