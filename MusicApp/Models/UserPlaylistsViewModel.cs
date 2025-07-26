using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace MusicApp.Models
{
    public class UserPlaylistsViewModel
    {
        public string UserName { get; set; } // Biệt danh của người dùng
        public string HinhAnh { get; set; } // Hình ảnh đại diện của người dùng
        public List<PlayList> PlayLists { get; set; } // Danh sách các playlist của người dùng

        public UserPlaylistsViewModel()
        {
            PlayLists = new List<PlayList>(); // Khởi tạo danh sách
        }
    }
}