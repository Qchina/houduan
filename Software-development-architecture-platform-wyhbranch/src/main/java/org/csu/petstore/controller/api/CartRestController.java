package org.csu.petstore.controller.api;

import io.swagger.annotations.*;
import org.csu.petstore.common.ApiResponse;
import org.csu.petstore.entity.Cart;
import org.csu.petstore.service.CartService;
import org.csu.petstore.vo.AccountVO;
import org.csu.petstore.vo.CartItem;
import org.csu.petstore.vo.CartItemListMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "购物车API", description = "购物车相关操作")
@RestController
@RequestMapping("/api/cart")
@Validated
public class CartRestController {

    @Autowired
    private CartService cartService;

    @Autowired
    private HttpSession session;

    /**
     * 获取购物车内容
     */
    @ApiOperation(value = "获取购物车", notes = "根据用户名获取购物车内容")
    @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String", paramType = "query")
    @GetMapping
    public ResponseEntity<ApiResponse> getCart() {
        //AccountVO account = (AccountVO) session.getAttribute("account");
        //String userId = account.getUsername();

        CartItemListMapper cartItemListMapper = cartService.getCartItemListByUsername("j2ee");
        List<CartItem> cartItemList = cartItemListMapper.getCartItemList();
        BigDecimal subTotal = BigDecimal.ZERO;
        int quantity = 0;
        for (CartItem cartItem : cartItemList) {
            if (cartItem.getCart().getIsRemoved() == 0) {
                subTotal = subTotal.add(cartItem.getTotal());
                quantity += cartItem.getCart().getQuantity();
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("items", cartItemList);

        System.out.println(result);
        return new ResponseEntity<>(new ApiResponse(true, "购物车获取成功", result), HttpStatus.OK);
    }

    /**
     * 添加商品到购物车
     */
    @ApiOperation(value = "添加商品到购物车", notes = "根据用户名和商品ID添加商品到购物车")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "itemId", value = "商品ID", required = true, dataType = "String", paramType = "query")
    })
    @PostMapping("/items")
    public ResponseEntity<ApiResponse> addItemToCart(@RequestParam String username, 
                                                  @RequestParam String itemId) {
        cartService.addItemToCart(username, itemId);
        CartItemListMapper cartItemListMapper = cartService.getCartItemListByUsername(username);
        
        return new ResponseEntity<>(new ApiResponse(true, "商品已添加到购物车", cartItemListMapper), HttpStatus.CREATED);
    }

    /**
     * 更新购物车商品数量
     */
    @ApiOperation(value = "更新购物车商品数量", notes = "更新购物车中指定商品的数量")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "itemId", value = "商品ID", required = true, dataType = "String", paramType = "path"),
        @ApiImplicitParam(name = "quantity", value = "数量", required = true, dataType = "int", paramType = "query")
    })
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse> updateCartItem(
                                                   @PathVariable String itemId,
                                                   @RequestParam int quantity) {
        AccountVO account = (AccountVO) session.getAttribute("account");
        String userId = account.getUsername();

        cartService.updateCartItemQuantity(userId, itemId, quantity);
        CartItemListMapper cartItemListMapper = cartService.getCartItemListByUsername(userId);
        
        return new ResponseEntity<>(new ApiResponse(true, "购物车已更新", cartItemListMapper), HttpStatus.OK);
    }

    /**
     * 批量更新购物车
     */
    @ApiOperation(value = "批量更新购物车", notes = "批量更新购物车中多个商品的数量")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "itemIds", value = "商品ID列表", required = true, dataType = "List", paramType = "query", allowMultiple = true),
        @ApiImplicitParam(name = "quantities", value = "数量列表", required = true, dataType = "List", paramType = "query", allowMultiple = true)
    })
    @PutMapping
    public ResponseEntity<ApiResponse> updateCart(@RequestParam String username,
                                               @RequestParam List<String> itemIds,
                                               @RequestParam List<Integer> quantities) {
        for (String itemId : itemIds) {
            int index = itemIds.indexOf(itemId);
            int quantity = quantities.get(index);
            cartService.updateCartItemQuantity(username, itemId, quantity);
        }
        CartItemListMapper cartItemListMapper = cartService.getCartItemListByUsername(username);
        List<CartItem> cartItemList = cartItemListMapper.getCartItemList();
        cartService.updateCartByCartItemList(cartItemList, username);
        
        return new ResponseEntity<>(new ApiResponse(true, "购物车批量更新成功", cartItemListMapper), HttpStatus.OK);
    }

    /**
     * 从购物车中移除商品
     */
    @ApiOperation(value = "从购物车中移除商品", notes = "从购物车中移除指定商品")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "itemId", value = "商品ID", required = true, dataType = "String", paramType = "path")
    })
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse> removeCartItem(
                                                   @PathVariable String itemId) {
        AccountVO account = (AccountVO) session.getAttribute("account");
        String userId = account.getUsername();
        
        cartService.removeItem(userId, itemId);
        CartItemListMapper cartItemListMapper = cartService.getCartItemListByUsername(userId);
        
        return new ResponseEntity<>(new ApiResponse(true, "商品已从购物车移除", cartItemListMapper), HttpStatus.OK);
    }

    /**
     * 清空购物车
     */
    @ApiOperation(value = "清空购物车", notes = "清空用户的购物车")
    @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String", paramType = "query")
    @DeleteMapping
    public ResponseEntity<ApiResponse> clearCart(@RequestParam String username) {
        cartService.clearCart(username);
        return new ResponseEntity<>(new ApiResponse(true, "购物车已清空", null), HttpStatus.OK);
    }
} 